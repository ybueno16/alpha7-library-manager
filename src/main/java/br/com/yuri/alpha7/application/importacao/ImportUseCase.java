package br.com.yuri.alpha7.application.importacao;

import br.com.yuri.alpha7.application.UnitOfWork;
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
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Caso de uso para importação em lote de livros a partir de arquivos externos.
 *
 * <p>Suporta múltiplos formatos através do padrão Strategy: cada {@link ImportParser}
 * registrado declara a extensão que suporta e é selecionado automaticamente com base
 * no nome do arquivo recebido. Novos formatos podem ser adicionados sem alterar esta classe.
 *
 * <p>Cada registro é processado dentro de uma transação independente: falhas individuais
 * são registradas no {@link ImportResult} sem interromper o restante da importação.
 * Livros já presentes no acervo ativo são ignorados; livros novos ou reativados são inseridos.
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
     * Importa livros a partir de um arquivo, selecionando o parser pela extensão do nome.
     *
     * @param stream   stream do arquivo
     * @param filename nome do arquivo (usado para detectar o formato pela extensão)
     * @return resultado com contadores de importados, ignorados e erros por linha
     * @throws ImportException se o formato do arquivo não for suportado
     */
    public ImportResult importFile(InputStream stream, String filename) {
        String ext = extension(filename);
        ImportParser parser = parsers.get(ext);
        if (parser == null) {
            throw new ImportException("Formato não suportado: ." + ext + ". Utilize CSV ou XML.");
        }

        logger.info("Importação iniciada: '{}'", filename);
        ImportResult result = new ImportResult();
        List<ImportRecord> records = parser.parse(stream);
        int lineNumber = 2;

        for (ImportRecord record : records) {
            final int line = lineNumber++;
            logger.debug("Processando linha {}", line);
            try {
                boolean[] imported = {false};
                unitOfWork.execute(() -> {
                    imported[0] = processRecord(record);
                });
                if (imported[0]) {
                    result.incrementNew();
                } else {
                    result.incrementSkipped();
                }
            } catch (IsbnInvalidoException e) {
                logger.warn("Erro na linha {}: ISBN inválido — {}", line, e.getMessage());
                result.addError("Linha " + line + ": ISBN inválido — " + e.getMessage());
            } catch (ImportException e) {
                logger.warn("Erro na linha {}: {}", line, e.getMessage());
                result.addError("Linha " + line + ": " + e.getMessage());
            } catch (Exception e) {
                logger.warn("Erro inesperado na linha {}: {}", line, e.getMessage());
                result.addError("Linha " + line + ": Não foi possível salvar o registro");
            }
        }

        logger.info("Importação concluída: {} importado(s), {} ignorado(s), {} erro(s)",
                result.getTotalNew(), result.getTotalSkipped(), result.getErrors().size());
        return result;
    }

    /**
     * Retorna {@code true} se o livro foi importado, {@code false} se já existe no acervo ativo.
     */
    private boolean processRecord(ImportRecord record) {
        ISBN isbn = new ISBN(record.getIsbn());

        if (livroRepository.findByIsbn(isbn).isPresent()) {
            return false;
        }

        Optional<Livro> existing = livroRepository.findByIsbnIncludingDeleted(isbn);

        Editora editora = editoraRepository.findByNome(record.getEditora())
                .orElseGet(() -> editoraRepository.save(new Editora(record.getEditora())));

        List<Autor> authors = resolveAuthors(record.getAutores());

        Livro livro = existing.orElseGet(() -> {
            Livro novo = new Livro();
            novo.setIsbn(isbn);
            return novo;
        });

        livro.setTitulo(record.getTitulo());
        livro.setEditora(editora);
        livro.setAutores(authors);
        assignOptionalFields(livro, record.getDataPublicacao(), record.getIdioma(), record.getNumeroPaginas());
        livroRepository.save(livro);

        return true;
    }

    private List<Autor> resolveAuthors(String authorNames) {
        List<Autor> authors = new ArrayList<>();
        for (String name : authorNames.split(",")) {
            String trimmed = name.trim();
            if (trimmed.isEmpty()) continue;
            Autor autor = autorRepository.findByNome(trimmed)
                    .orElseGet(() -> autorRepository.save(new Autor(trimmed)));
            authors.add(autor);
        }
        return authors;
    }

    private void assignOptionalFields(Livro livro, String dataPublicacao, String idioma, String numeroPaginas) {
        if (!dataPublicacao.isEmpty()) {
            try {
                livro.setDataPublicacao(LocalDate.parse(dataPublicacao));
            } catch (DateTimeParseException e) {
                throw new ImportException("Data no formato inválido. Use YYYY-MM-DD (ex: 2024-01-31)");
            }
        }
        if (!idioma.isEmpty()) {
            livro.setIdioma(idioma);
        }
        if (!numeroPaginas.isEmpty()) {
            try {
                livro.setNumeroPaginas(Integer.parseInt(numeroPaginas));
            } catch (NumberFormatException e) {
                throw new ImportException("Número de páginas inválido. Informe apenas números inteiros");
            }
        }
    }

    private String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase() : "";
    }
}
