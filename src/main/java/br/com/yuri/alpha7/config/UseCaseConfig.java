package br.com.yuri.alpha7.config;

import br.com.yuri.alpha7.application.editora.EditoraUseCase;
import br.com.yuri.alpha7.application.importacao.CsvImportParser;
import br.com.yuri.alpha7.application.importacao.ImportUseCase;
import br.com.yuri.alpha7.application.importacao.XmlImportParser;
import br.com.yuri.alpha7.application.isbn.IsbnLookupUseCase;
import br.com.yuri.alpha7.application.livro.BookCrudUseCase;
import br.com.yuri.alpha7.application.livro.BookExportUseCase;
import br.com.yuri.alpha7.application.livro.BookSearchUseCase;

import java.util.Arrays;

public class UseCaseConfig {

    private final IsbnLookupUseCase isbnLookup;
    private final BookCrudUseCase   bookCrud;
    private final BookSearchUseCase bookSearch;
    private final BookExportUseCase bookExport;
    private final ImportUseCase     importUseCase;
    private final EditoraUseCase    editoraUseCase;

    public UseCaseConfig(InfrastructureConfig infra, RepositoryConfig repos) {
        this.isbnLookup = new IsbnLookupUseCase(
                repos.livroRepository(),
                infra.openLibraryClient()
        );
        this.bookCrud      = new BookCrudUseCase(repos.livroRepository());
        this.bookSearch    = new BookSearchUseCase(repos.livroRepository());
        this.bookExport    = new BookExportUseCase(repos.livroRepository());
        this.editoraUseCase = new EditoraUseCase(repos.editoraRepository());
        this.importUseCase = new ImportUseCase(
                repos.unitOfWork(),
                repos.livroRepository(),
                repos.autorRepository(),
                repos.editoraRepository(),
                Arrays.asList(new CsvImportParser(), new XmlImportParser())
        );
    }

    public IsbnLookupUseCase isbnLookup()      { return isbnLookup; }
    public BookCrudUseCase   bookCrud()         { return bookCrud; }
    public BookSearchUseCase bookSearch()       { return bookSearch; }
    public BookExportUseCase bookExport()       { return bookExport; }
    public ImportUseCase     importUseCase()    { return importUseCase; }
    public EditoraUseCase    editoraUseCase()   { return editoraUseCase; }
}
