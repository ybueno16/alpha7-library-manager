package br.com.yuri.alpha7.application.livro;

import br.com.yuri.alpha7.domain.autor.model.Autor;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.repository.LivroRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Caso de uso responsável por exportar o acervo de livros para um arquivo CSV.
 *
 * <p>O formato gerado é compatível com o importador {@link br.com.yuri.alpha7.application.importacao.CsvImportParser}:
 * o cabeçalho e a ordem das colunas são idênticos, permitindo que um arquivo exportado
 * possa ser editado e reimportado sem modificações estruturais.
 */
public class BookExportUseCase {

    private final LivroRepository livroRepository;

    public BookExportUseCase(LivroRepository livroRepository) {
        this.livroRepository = livroRepository;
    }

    /**
     * Exporta todos os livros do acervo para o writer fornecido em formato CSV.
     *
     * @param writer destino da escrita (arquivo, buffer, etc.)
     * @return quantidade de livros exportados
     * @throws IOException em caso de erro de escrita
     */
    public int exportToCsv(Writer writer) throws IOException {
        List<Livro> livros = livroRepository.findAll();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("titulo", "isbn", "autores", "editora",
                           "dataPublicacao", "idioma", "numeroPaginas")
                .build();

        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            for (Livro livro : livros) {
                printer.printRecord(
                        livro.getTitulo(),
                        livro.getIsbn().getValue(),
                        livro.getAutores().stream()
                                .map(Autor::getNome)
                                .collect(Collectors.joining(", ")),
                        livro.getEditora() != null ? livro.getEditora().getNome() : "",
                        livro.getDataPublicacao() != null ? livro.getDataPublicacao().toString() : "",
                        livro.getIdioma() != null ? livro.getIdioma() : "",
                        livro.getNumeroPaginas() != null ? livro.getNumeroPaginas().toString() : ""
                );
            }
        }

        return livros.size();
    }
}
