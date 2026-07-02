package br.com.yuri.alpha7.application.importacao;

import br.com.yuri.alpha7.application.UnitOfWork;
import br.com.yuri.alpha7.application.importacao.model.ImportPreviewRecord;
import br.com.yuri.alpha7.application.importacao.model.ImportRecord;
import br.com.yuri.alpha7.application.importacao.model.ImportResult;
import br.com.yuri.alpha7.application.importacao.parser.ImportParser;
import br.com.yuri.alpha7.domain.autor.model.Autor;
import br.com.yuri.alpha7.domain.autor.repository.AutorRepository;
import br.com.yuri.alpha7.domain.editora.model.Editora;
import br.com.yuri.alpha7.domain.editora.repository.EditoraRepository;
import br.com.yuri.alpha7.domain.exception.ImportException;
import br.com.yuri.alpha7.domain.exception.IsbnInvalidoException;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.repository.LivroRepository;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Caso de uso para importação em lote de livros a partir de arquivos externos.
 *
 * <p>A importação segue duas fases separadas:
 * <ol>
 *   <li>{@link #preview(InputStream, String)} — faz o parse e a validação sem gravar nada,
 *       retornando uma lista de {@link ImportPreviewRecord} com o status de cada linha.</li>
 *   <li>{@link #importSelected(List)} — grava apenas os registros marcados pelo usuário.</li>
 * </ol>
 *
 * <p>Suporta múltiplos formatos através do padrão Strategy: cada {@link ImportParser}
 * registrado declara a extensão que suporta e é selecionado automaticamente com base
 * no nome do arquivo.
 */
public class ImportUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ImportUseCase.class);

    private final UnitOfWork unitOfWork;
    private final LivroRepository livroRepository;
    private final AutorRepository autorRepository;
    private final EditoraRepository editoraRepository;
    private final Map<String, ImportParser> parsers = new HashMap<>();

    public ImportUseCase(
            UnitOfWork unitOfWork,
            LivroRepository livroRepository,
            AutorRepository autorRepository,
            EditoraRepository editoraRepository,
            List<ImportParser> parsers
    ) {
        this.unitOfWork        = unitOfWork;
        this.livroRepository   = livroRepository;
        this.autorRepository   = autorRepository;
        this.editoraRepository = editoraRepository;
        parsers.forEach(p -> this.parsers.put(p.supports(), p));
    }

    /**
     * Faz o parse e a validação do arquivo sem gravar nenhum dado.
     *
     * @param stream   stream do arquivo
     * @param filename nome do arquivo (usado para detectar o formato pela extensão)
     * @return lista com o status de cada linha: NOVO, JA_EXISTE ou ERRO
     * @throws ImportException se o formato do arquivo não for suportado
     */
    public List<ImportPreviewRecord> preview(InputStream stream, String filename) {
        String ext = extension(filename);
        ImportParser parser = parsers.get(ext);
        if (parser == null) {
            throw new ImportException("Formato não suportado: ." + ext + ". Utilize CSV ou XML.");
        }

        logger.info("Preview iniciado: '{}'", filename);
        List<ImportRecord> records = parser.parse(stream);
        List<ImportPreviewRecord> previews = new ArrayList<>();
        // CSV: starts at line 2 (line 1 is the header); XML: estimated from element index (no real line number)
        int lineNumber = "csv".equals(ext) ? 2 : 1;

        for (ImportRecord record : records) {
            int line = lineNumber++;
            previews.add(validateRecord(line, record));
        }

        return previews;
    }

    /**
     * Grava apenas os registros marcados como selecionados na lista de preview.
     * Registros com status {@link ImportPreviewRecord.Status#ERRO} são ignorados mesmo se marcados.
     *
     * @param previews lista retornada por {@link #preview(InputStream, String)}
     * @return resultado com contadores de importados, ignorados e erros
     */
    public ImportResult importSelected(List<ImportPreviewRecord> previews) {
        return importSelected(previews, null);
    }

    public ImportResult importSelected(List<ImportPreviewRecord> previews, Consumer<Integer> onProgress) {
        ImportResult result = new ImportResult();

        for (ImportPreviewRecord preview : previews) {
            if (preview.getStatus() == ImportPreviewRecord.Status.ERRO) {
                result.addError("Linha " + preview.getLineNumber() + ": " + preview.getMensagem());
                continue;
            }
            if (!preview.isSelecionado()) {
                result.incrementSkipped();
                continue;
            }
            try {
                unitOfWork.execute(() -> saveRecord(preview.getSourceRecord()));
                result.incrementNew();
                if (onProgress != null) {
                    onProgress.accept(result.getTotalNew());
                }
            } catch (IsbnInvalidoException e) {
                logger.warn("Erro na linha {}: ISBN inválido — {}", preview.getLineNumber(), e.getMessage());
                result.addError("Linha " + preview.getLineNumber() + ": ISBN inválido — " + e.getMessage());
            } catch (ImportException e) {
                logger.warn("Erro na linha {}: {}", preview.getLineNumber(), e.getMessage());
                result.addError("Linha " + preview.getLineNumber() + ": " + e.getMessage());
            } catch (Exception e) {
                logger.warn("Erro inesperado na linha {}: {}", preview.getLineNumber(), e.getMessage());
                result.addError("Linha " + preview.getLineNumber() + ": Não foi possível salvar o registro");
            }
        }

        logger.info("Importação concluída: {} importado(s), {} ignorado(s), {} erro(s)",
                result.getTotalNew(), result.getTotalSkipped(), result.getErrors().size());
        return result;
    }

    private ImportPreviewRecord validateRecord(int line, ImportRecord record) {
        try {
            if (record.getTitulo() == null || record.getTitulo().trim().isEmpty()) {
                return new ImportPreviewRecord(
                        line, record.getTitulo(), record.getIsbn(),
                        ImportPreviewRecord.Status.ERRO,
                        "Título não pode ser vazio",
                        false,
                        record
                );
            }
            if (!hasValidAuthor(record.getAutores())) {
                return new ImportPreviewRecord(
                        line, record.getTitulo(), record.getIsbn(),
                        ImportPreviewRecord.Status.ERRO,
                        "Informe ao menos um autor válido",
                        false,
                        record
                );
            }

            ISBN isbn = new ISBN(record.getIsbn());
            validateOptionalFields(record.getDataPublicacao(), record.getNumeroPaginas());

            if (livroRepository.findByIsbn(isbn).isPresent()) {
                return new ImportPreviewRecord(
                        line, record.getTitulo(), record.getIsbn(),
                        ImportPreviewRecord.Status.JA_EXISTE,
                        "Já existe no acervo",
                        false,
                        record
                );
            }

            return new ImportPreviewRecord(
                    line, record.getTitulo(), record.getIsbn(),
                    ImportPreviewRecord.Status.NOVO,
                    "Será importado",
                    true,
                    record
            );

        } catch (IsbnInvalidoException e) {
            return new ImportPreviewRecord(
                    line, record.getTitulo(), record.getIsbn(),
                    ImportPreviewRecord.Status.ERRO,
                    "ISBN inválido — " + e.getMessage(),
                    false,
                    record
            );
        } catch (ImportException e) {
            return new ImportPreviewRecord(
                    line, record.getTitulo(), record.getIsbn(),
                    ImportPreviewRecord.Status.ERRO,
                    e.getMessage(),
                    false,
                    record
            );
        }
    }

    private void validateOptionalFields(String dataPublicacao, String numeroPaginas) {
        if (dataPublicacao != null && !dataPublicacao.isEmpty()) validateDate(dataPublicacao);
        if (numeroPaginas != null && !numeroPaginas.isEmpty()) validatePages(numeroPaginas);
    }

    private void validateDate(String value) {
        if (!value.matches("\\d{4}")) {
            throw new ImportException("Ano de publicação inválido. Use YYYY (ex: 2024)");
        }
        int year;
        try {
            year = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ImportException("Ano de publicação inválido. Use YYYY (ex: 2024)");
        }
        if (year < 1 || year > LocalDate.now().getYear()) {
            throw new ImportException("Ano de publicação deve estar entre 0001 e o ano atual");
        }
    }

    private void validatePages(String value) {
        int pages;
        try {
            pages = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ImportException("Número de páginas inválido. Informe apenas números inteiros");
        }
        if (pages <= 0) {
            throw new ImportException("Número de páginas deve ser maior que zero");
        }
    }

    private void saveRecord(ImportRecord record) {
        ISBN isbn = new ISBN(record.getIsbn());

        Optional<Livro> existing = livroRepository.findByIsbnIncludingDeleted(isbn);

        Livro livro = existing.orElseGet(() -> {
            Livro novo = new Livro();
            novo.setIsbn(isbn);
            return novo;
        });

        String editoraNome = record.getEditora() == null ? "" :
                record.getEditora().trim();

        if (!editoraNome.isEmpty()) {
            Editora editora = editoraRepository.findByNome(editoraNome)
                    .orElseGet(() -> editoraRepository.save(new Editora(editoraNome)));
            livro.setEditora(editora);
        }


        List<Autor> authors = resolveAuthors(record.getAutores());


        livro.setTitulo(record.getTitulo());
        livro.setAutores(authors);
        assignFields(livro, record.getDataPublicacao(), record.getIdioma(), record.getNumeroPaginas());
        livroRepository.save(livro);
    }

    private List<Autor> resolveAuthors(String authorNames) {
        List<Autor> authors = new ArrayList<>();
        for (String trimmed : parseAuthorNames(authorNames)) {
            Autor autor = autorRepository.findByNome(trimmed)
                    .orElseGet(() -> autorRepository.save(new Autor(trimmed)));
            authors.add(autor);
        }
        return authors;
    }

    private boolean hasValidAuthor(String authorNames) {
        return !parseAuthorNames(authorNames).isEmpty();
    }

    private List<String> parseAuthorNames(String authorNames) {
        List<String> names = new ArrayList<>();
        if (authorNames == null) {
            return names;
        }
        for (String name : authorNames.split(";")) {
            String trimmed = name.trim();
            if (!trimmed.isEmpty()) {
                names.add(trimmed);
            }
        }
        return names;
    }

    private void assignFields(Livro livro, String dataPublicacao, String idioma, String numeroPaginas) {
        if (dataPublicacao != null && !dataPublicacao.isEmpty()) {
            livro.setDataPublicacao(LocalDate.of(Integer.parseInt(dataPublicacao), 1, 1));
        }
        if (idioma != null && !idioma.isEmpty()) {
            livro.setIdioma(idioma);
        }
        if (numeroPaginas != null && !numeroPaginas.isEmpty()) {
            livro.setNumeroPaginas(Integer.parseInt(numeroPaginas));
        }
    }

    private String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase() : "";
    }
}
