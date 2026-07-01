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

        Livro found = useCase.findById(saved.getId());

        assertEquals("Clean Code", found.getTitulo());
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

        Livro updated = useCase.findById(saved.getId());
        assertEquals("Updated Title", updated.getTitulo());
    }

    @Test
    @DisplayName(
            "Given a book saved with a publisher name," +
            " when saveWithEditora is called," +
            " then the book is persisted with the editora linked"
    )
    void shouldSaveBookWithEditoraWhenNameIsProvided() {
        Livro saved = useCase.saveWithEditora(bookWithIsbn("Effective Java", ISBN_A), "Addison-Wesley");

        Livro found = useCase.findById(saved.getId());

        assertNotNull(found.getEditora());
        assertEquals("Addison-Wesley", found.getEditora().getNome());
    }

    @Test
    @DisplayName(
            "Given two books saved with the same publisher name," +
            " when saveWithEditora is called for each," +
            " then both reference the same editora record"
    )
    void shouldReuseExistingEditoraOnSecondSave() {
        Livro first  = useCase.saveWithEditora(bookWithIsbn("Effective Java",  ISBN_A), "Addison-Wesley");
        Livro second = useCase.saveWithEditora(bookWithIsbn("Design Patterns", ISBN_B), "Addison-Wesley");

        assertEquals(first.getEditora().getId(), second.getEditora().getId());
    }

    @Test
    @DisplayName(
            "Given a book with no publisher name," +
            " when saveWithEditora is called with null," +
            " then the book is saved without editora"
    )
    void shouldSaveBookWithoutEditoraWhenNameIsNull() {
        Livro saved = useCase.saveWithEditora(bookWithIsbn("Anonymous", ISBN_A), null);

        Livro found = useCase.findById(saved.getId());

        assertNull(found.getEditora());
    }

    @Test
    @DisplayName(
            "Given a non-existent book id," +
            " when delete is called," +
            " then BookNotFoundException is thrown"
    )
    void shouldThrowWhenDeletingNonExistentBook() {
        assertThrows(BookNotFoundException.class, () -> useCase.delete(99999L));
    }

    private Livro bookWithIsbn(String titulo, ISBN isbn) {
        Livro livro = new Livro();
        livro.setTitulo(titulo);
        livro.setIsbn(isbn);
        return livro;
    }
}
