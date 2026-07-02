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
        Map<String, Integer> isbnsVistos = new HashMap<>();
        int lineNumber = "csv".equals(ext) ? 2 : 1;

        for (ImportRecord record : records) {
            int line = lineNumber++;
            previews.add(validateRecord(line, record, isbnsVistos));
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

    /**
     * Grava apenas os registros marcados como selecionados na lista de preview, notificando
     * o progresso a cada linha importada com sucesso.
     *
     * @param previews   lista retornada por {@link #preview(InputStream, String)}
     * @param onProgress callback chamado com o total de registros já importados após cada
     *                   linha bem-sucedida; pode ser {@code null}
     * @return resultado com contadores de importados, ignorados e erros
     */
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

    /**
     * Valida uma linha do arquivo e classifica seu status sem gravar nada.
     *
     * <p>A ordem de checagem é: título obrigatório, autor obrigatório, ISBN válido, campos
     * opcionais (data e número de páginas), ISBN duplicado dentro do próprio arquivo — usando
     * {@code isbnsVistos} para lembrar a primeira linha em que cada ISBN normalizado apareceu —
     * e por fim se o ISBN já existe no acervo.
     *
     * @param line        número da linha no arquivo original, usado nas mensagens de erro
     * @param record      dados brutos lidos pelo {@link ImportParser}
     * @param isbnsVistos mapa de ISBN normalizado para a primeira linha em que ele apareceu
     *                    nesta mesma chamada de {@link #preview(InputStream, String)}
     * @return preview com o status {@code NOVO}, {@code JA_EXISTE} ou {@code ERRO} da linha
     */
    private ImportPreviewRecord validateRecord(int line, ImportRecord record, Map<String, Integer> isbnsVistos) {
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

            Integer firstLine = isbnsVistos.get(isbn.getValue());
            if (firstLine != null) {
                return new ImportPreviewRecord(
                        line, record.getTitulo(), record.getIsbn(),
                        ImportPreviewRecord.Status.ERRO,
                        "ISBN duplicado no arquivo — já aparece na linha " + firstLine,
                        false,
                        record
                );
            }
            isbnsVistos.put(isbn.getValue(), line);

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

    /**
     * Valida os campos opcionais da linha, se informados.
     *
     * @param dataPublicacao ano de publicação em texto, ou vazio/nulo se omitido
     * @param numeroPaginas  número de páginas em texto, ou vazio/nulo se omitido
     * @throws ImportException se algum campo informado for inválido
     */
    private void validateOptionalFields(String dataPublicacao, String numeroPaginas) {
        if (dataPublicacao != null && !dataPublicacao.isEmpty()) validateDate(dataPublicacao);
        if (numeroPaginas != null && !numeroPaginas.isEmpty()) validatePages(numeroPaginas);
    }

    /**
     * Valida que o valor representa um ano no formato {@code yyyy}, entre 1 e o ano atual.
     *
     * @param value ano de publicação em texto
     * @throws ImportException se o valor não for um ano válido nesse intervalo
     */
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

    /**
     * Valida que o valor representa um número inteiro positivo de páginas.
     *
     * @param value número de páginas em texto
     * @throws ImportException se o valor não for um inteiro positivo
     */
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

    /**
     * Persiste um registro já validado, fazendo upsert por ISBN: reativa e atualiza o livro
     * se já existir (inclusive soft-deletado), ou cria um novo caso contrário. Autor e editora
     * são resolvidos por nome, reaproveitando os já cadastrados.
     *
     * @param record dado bruto da linha, obtido do preview selecionado pelo usuário
     */
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

    /**
     * Resolve os nomes de autores separados por ponto-e-vírgula em entidades de domínio,
     * reaproveitando autores já cadastrados pelo nome e criando os que não existem.
     *
     * @param authorNames texto com nomes de autores separados por {@code ;}
     * @return autores resolvidos, na mesma ordem em que aparecem no texto
     */
    private List<Autor> resolveAuthors(String authorNames) {
        List<Autor> authors = new ArrayList<>();
        for (String trimmed : parseAuthorNames(authorNames)) {
            Autor autor = autorRepository.findByNome(trimmed)
                    .orElseGet(() -> autorRepository.save(new Autor(trimmed)));
            authors.add(autor);
        }
        return authors;
    }

    /**
     * Verifica se o texto de autores contém ao menos um nome não vazio após o parse.
     *
     * @param authorNames texto com nomes de autores separados por {@code ;}
     * @return {@code true} se houver ao menos um autor válido
     */
    private boolean hasValidAuthor(String authorNames) {
        return !parseAuthorNames(authorNames).isEmpty();
    }

    /**
     * Separa o texto de autores por {@code ;}, removendo espaços e entradas vazias.
     *
     * @param authorNames texto com nomes de autores separados por {@code ;}, ou {@code null}
     * @return nomes de autores não vazios já sem espaços nas bordas; lista vazia se {@code null}
     */
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

    /**
     * Atribui os campos opcionais já validados ao livro. Campos vazios ou nulos são ignorados,
     * mantendo o valor atual do livro (relevante em upserts, onde o livro pode já ter dados).
     *
     * @param livro          livro a ser preenchido
     * @param dataPublicacao ano de publicação em texto, ou vazio/nulo se omitido
     * @param idioma         idioma do livro, ou vazio/nulo se omitido
     * @param numeroPaginas  número de páginas em texto, ou vazio/nulo se omitido
     */
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

    /**
     * Extrai a extensão do nome do arquivo, em minúsculas e sem o ponto.
     *
     * @param filename nome do arquivo
     * @return extensão em minúsculas, ou string vazia se não houver ponto no nome
     */
    private String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase() : "";
    }
}
