package br.com.yuri.alpha7.application.livro;

import br.com.yuri.alpha7.application.importacao.parser.CsvImportParser;
import br.com.yuri.alpha7.domain.autor.model.Autor;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.repository.LivroRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Caso de uso responsável por exportar o acervo de livros para um arquivo CSV.
 *
 * <p>O formato gerado é compatível com o importador {@link CsvImportParser}:
 * o cabeçalho e a ordem das colunas são idênticos, permitindo que um arquivo exportado
 * possa ser editado e reimportado sem modificações estruturais.
 *
 * <p>O {@code writer} passado para {@link #exportToCsv(Writer)} pertence ao chamador —
 * este caso de uso faz flush mas não fecha o writer ao finalizar.
 */
public class BookExportUseCase {

    private static final Logger logger = LoggerFactory.getLogger(BookExportUseCase.class);

    private final LivroRepository livroRepository;

    public BookExportUseCase(LivroRepository livroRepository) {
        this.livroRepository = livroRepository;
    }

    /**
     * Exporta todos os livros do acervo para o writer fornecido em formato CSV.
     *
     * @param writer destino da escrita (pertence ao chamador — não é fechado aqui)
     * @return quantidade de livros exportados
     * @throws IOException em caso de erro de escrita
     */
    public int exportToCsv(Writer writer) throws IOException {
        return exportToCsv(writer, null);
    }

    public int exportToCsv(Writer writer, BiConsumer<Integer, Integer> onProgress) throws IOException {
        List<Livro> livros = livroRepository.findAll();
        int total = livros.size();
        logger.info("Exportando {} livro(s) para CSV.", total);

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("titulo", "isbn", "autores", "editora",
                           "dataPublicacao", "idioma", "numeroPaginas")
                .build();

        CSVPrinter printer = new CSVPrinter(writer, format);
        int count = 0;
        for (Livro livro : livros) {
            printer.printRecord(
                    livro.getTitulo(),
                    livro.getIsbn().getValue(),
                    livro.getAutores().stream()
                            .map(Autor::getNome)
                            .collect(Collectors.joining("; ")),
                    livro.getEditora() != null ? livro.getEditora().getNome() : "",
                    livro.getDataPublicacao() != null ? livro.getDataPublicacao().toString() : "",
                    livro.getIdioma() != null ? livro.getIdioma() : "",
                    livro.getNumeroPaginas() != null ? livro.getNumeroPaginas().toString() : ""
            );
            if (onProgress != null) {
                onProgress.accept(++count, total);
            }
        }
        printer.flush();
        return total;
    }
}
