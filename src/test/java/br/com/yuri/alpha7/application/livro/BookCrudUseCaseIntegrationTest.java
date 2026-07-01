package br.com.yuri.alpha7.application.livro;

import br.com.yuri.alpha7.domain.exception.BookNotFoundException;
import br.com.yuri.alpha7.domain.exception.IsbnInvalidoException;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import br.com.yuri.alpha7.infra.persistence.AbstractRepositoryTest;
import br.com.yuri.alpha7.infra.persistence.HibernateUnitOfWork;
import br.com.yuri.alpha7.infra.persistence.editora.EditoraRepositoryImpl;
import br.com.yuri.alpha7.infra.persistence.livro.LivroRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BookCrudUseCaseIntegrationTest extends AbstractRepositoryTest {

    private final LivroRepositoryImpl   livroRepository   = new LivroRepositoryImpl();
    private final EditoraRepositoryImpl editoraRepository = new EditoraRepositoryImpl();
    private final HibernateUnitOfWork   unitOfWork        = new HibernateUnitOfWork();
    private final BookCrudUseCase       useCase           = new BookCrudUseCase(livroRepository, editoraRepository, unitOfWork);

    private static final ISBN ISBN_A = new ISBN("9780132350884");
    private static final ISBN ISBN_B = new ISBN("9780134685991");

    @Test
    @DisplayName(
            "Given a new book is saved via the use case," +
            " when findById is called with the generated id," +
            " then the book is returned"
    )
    void shouldFindBookAfterSave() {
        Livro saved = useCase.save(bookWithIsbn("Clean Code", ISBN_A));

        Optional<Livro> found = useCase.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Clean Code", found.get().getTitulo());
    }

    @Test
    @DisplayName(
            "Given a book already in the collection," +
            " when a second book with the same ISBN is saved," +
            " then IsbnInvalidoException is thrown"
    )
    void shouldRejectDuplicateIsbn() {
        useCase.save(bookWithIsbn("Book A", ISBN_B));

        assertThrows(IsbnInvalidoException.class, () ->
                useCase.save(bookWithIsbn("Book B", ISBN_B))
        );
    }

    @Test
    @DisplayName(
            "Given an existing book that is deleted via the use case," +
            " when findById is called afterwards," +
            " then BookNotFoundException is thrown"
    )
    void shouldThrowBookNotFoundAfterDeletion() {
        Livro saved = useCase.save(bookWithIsbn("To Delete", ISBN_A));

        useCase.delete(saved.getId());

        assertThrows(BookNotFoundException.class, () -> useCase.findById(saved.getId()));
    }

    @Test
    @DisplayName(
            "Given an existing book being edited with the same ISBN," +
            " when save is called with its original id," +
            " then the book is updated without throwing"
    )
    void shouldAllowUpdatingExistingBookWithSameIsbn() {
        Livro saved = useCase.save(bookWithIsbn("Original Title", ISBN_A));
        saved.setTitulo("Updated Title");

        assertDoesNotThrow(() -> useCase.save(saved));

        Optional<Livro> updated = useCase.findById(saved.getId());
        assertTrue(updated.isPresent());
        assertEquals("Updated Title", updated.get().getTitulo());
    }

    private Livro bookWithIsbn(String titulo, ISBN isbn) {
        Livro livro = new Livro();
        livro.setTitulo(titulo);
        livro.setIsbn(isbn);
        return livro;
    }
}
