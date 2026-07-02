package br.com.yuri.alpha7.config;

import br.com.yuri.alpha7.application.importacao.parser.CsvImportParser;
import br.com.yuri.alpha7.application.importacao.ImportUseCase;
import br.com.yuri.alpha7.application.importacao.parser.XmlImportParser;
import br.com.yuri.alpha7.application.isbn.IsbnLookupUseCase;
import br.com.yuri.alpha7.application.livro.BookCrudUseCase;
import br.com.yuri.alpha7.application.livro.BookExportUseCase;
import br.com.yuri.alpha7.application.livro.BookSearchUseCase;
import br.com.yuri.alpha7.application.stats.AcervoStatsUseCase;

import java.util.Arrays;

/**
 * Wiring manual dos casos de uso da aplicação.
 *
 * <p>Instancia cada caso de uso a partir das dependências já construídas em
 * {@link InfrastructureConfig} e {@link RepositoryConfig}, servindo como ponto único
 * de montagem consumido pela camada de apresentação.
 */
public class UseCaseConfig {

    private final IsbnLookupUseCase  isbnLookup;
    private final BookCrudUseCase    bookCrud;
    private final BookSearchUseCase  bookSearch;
    private final BookExportUseCase  bookExport;
    private final ImportUseCase      importUseCase;
    private final AcervoStatsUseCase acervoStats;

    public UseCaseConfig(InfrastructureConfig infra, RepositoryConfig repos) {
        this.isbnLookup = new IsbnLookupUseCase(infra.openLibraryClient());
        this.bookCrud      = new BookCrudUseCase(repos.livroRepository(), repos.autorRepository(), repos.editoraRepository(), repos.unitOfWork());
        this.bookSearch    = new BookSearchUseCase(repos.livroRepository());
        this.bookExport    = new BookExportUseCase(repos.livroRepository());
        this.importUseCase = new ImportUseCase(
                repos.unitOfWork(),
                repos.livroRepository(),
                repos.autorRepository(),
                repos.editoraRepository(),
                Arrays.asList(new CsvImportParser(), new XmlImportParser())
        );
        this.acervoStats = new AcervoStatsUseCase(repos.livroRepository(), repos.unitOfWork());
    }

    /** @return caso de uso de busca de livro por ISBN na OpenLibrary */
    public IsbnLookupUseCase  isbnLookup()      { return isbnLookup; }

    /** @return caso de uso de criação, atualização e exclusão de livros */
    public BookCrudUseCase    bookCrud()         { return bookCrud; }

    /** @return caso de uso de busca e listagem paginada de livros */
    public BookSearchUseCase  bookSearch()       { return bookSearch; }

    /** @return caso de uso de exportação do acervo em CSV */
    public BookExportUseCase  bookExport()       { return bookExport; }

    /** @return caso de uso de importação em lote via CSV/XML */
    public ImportUseCase      importUseCase()    { return importUseCase; }

    /** @return caso de uso de estatísticas do acervo */
    public AcervoStatsUseCase acervoStats()      { return acervoStats; }
}
