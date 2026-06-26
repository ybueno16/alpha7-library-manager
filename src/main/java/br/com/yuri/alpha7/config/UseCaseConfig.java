package br.com.yuri.alpha7.config;

import br.com.yuri.alpha7.application.importacao.ImportUseCase;
import br.com.yuri.alpha7.application.isbn.IsbnLookupUseCase;
import br.com.yuri.alpha7.application.livro.BookCrudUseCase;
import br.com.yuri.alpha7.application.livro.BookSearchUseCase;
import br.com.yuri.alpha7.domain.editora.repository.EditoraRepository;

public class UseCaseConfig {

    private final IsbnLookupUseCase  isbnLookup;
    private final BookCrudUseCase    bookCrud;
    private final BookSearchUseCase  bookSearch;
    private final ImportUseCase      importUseCase;
    private final EditoraRepository  editoraRepository;

    public UseCaseConfig(InfrastructureConfig infra, RepositoryConfig repos) {
        this.isbnLookup = new IsbnLookupUseCase(
                repos.livroRepository(),
                infra.openLibraryClient()
        );
        this.bookCrud         = new BookCrudUseCase(repos.livroRepository());
        this.bookSearch       = new BookSearchUseCase(repos.livroRepository());
        this.editoraRepository = repos.editoraRepository();
        this.importUseCase    = new ImportUseCase(
                repos.unitOfWork(),
                repos.livroRepository(),
                repos.autorRepository(),
                repos.editoraRepository()
        );
    }

    public IsbnLookupUseCase isbnLookup()          { return isbnLookup; }
    public BookCrudUseCase   bookCrud()             { return bookCrud; }
    public BookSearchUseCase bookSearch()           { return bookSearch; }
    public ImportUseCase     importUseCase()        { return importUseCase; }
    public EditoraRepository editoraRepository()   { return editoraRepository; }
}
