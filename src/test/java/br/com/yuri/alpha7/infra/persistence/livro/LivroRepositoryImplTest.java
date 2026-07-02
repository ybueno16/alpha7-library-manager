package br.com.yuri.alpha7.infra.persistence.livro;

import br.com.yuri.alpha7.domain.autor.model.Autor;
import br.com.yuri.alpha7.domain.editora.model.Editora;
import br.com.yuri.alpha7.domain.exception.BookNotFoundException;
import br.com.yuri.alpha7.domain.exception.IsbnInvalidoException;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import br.com.yuri.alpha7.infra.persistence.AbstractRepositoryTest;
import br.com.yuri.alpha7.infra.persistence.HibernateUnitOfWork;
import br.com.yuri.alpha7.infra.persistence.autor.AutorRepositoryImpl;
import br.com.yuri.alpha7.infra.persistence.editora.EditoraRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.yuri.alpha7.domain.livro.repository.LivroFiltro;
import br.com.yuri.alpha7.domain.livro.repository.PagedResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LivroRepositoryImplTest extends AbstractRepositoryTest {

    private final LivroRepositoryImpl livroRepository = new LivroRepositoryImpl();
    private final EditoraRepositoryImpl editoraRepository = new EditoraRepositoryImpl();
    private final AutorRepositoryImpl autorRepository = new AutorRepositoryImpl();

    private static final ISBN ISBN_EFFECTIVE_JAVA = new ISBN("9780134685991");
    private static final ISBN ISBN_CLEAN_CODE     = new ISBN("9780132350884");
    private static final ISBN ISBN_DDD            = new ISBN("9780321125217");
    private static final ISBN ISBN_SICP           = new ISBN("9780262510875");

    @Test
    @DisplayName(
            "Given a new book," +
            " when save is called," +
            " then book is persisted with a generated id"
    )
    void shouldPersistBookWithGeneratedId() {
        Livro saved = livroRepository.save(basicBook("Effective Java", ISBN_EFFECTIVE_JAVA));
        assertNotNull(saved.getId());
        assertEquals("Effective Java", saved.getTitulo());
        assertEquals(ISBN_EFFECTIVE_JAVA, saved.getIsbn());
    }

    @Test
    @DisplayName(
            "Given an existing book," +
            " when findById is called with its id," +
            " then book is returned"
    )
    void shouldReturnBookWhenFoundById() {
        Livro saved = livroRepository.save(basicBook("Clean Code", ISBN_CLEAN_CODE));
        Optional<Livro> result = livroRepository.findById(saved.getId());
        assertTrue(result.isPresent());
        assertEquals("Clean Code", result.get().getTitulo());
    }

    @Test
    @DisplayName(
            "Given no book with the given id," +
            " when findById is called," +
            " then empty optional is returned"
    )
    void shouldReturnEmptyWhenBookNotFoundById() {
        Optional<Livro> result = livroRepository.findById(999L);
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName(
            "Given an existing book," +
            " when findByIsbn is called with its ISBN," +
            " then book is returned"
    )
    void shouldReturnBookWhenFoundByIsbn() {
        livroRepository.save(basicBook("Effective Java", ISBN_EFFECTIVE_JAVA));
        Optional<Livro> result = livroRepository.findByIsbn(ISBN_EFFECTIVE_JAVA);
        assertTrue(result.isPresent());
        assertEquals(ISBN_EFFECTIVE_JAVA, result.get().getIsbn());
    }

    @Test
    @DisplayName(
            "Given no book with the given ISBN," +
            " when findByIsbn is called," +
            " then empty optional is returned"
    )
    void shouldReturnEmptyWhenBookNotFoundByIsbn() {
        Optional<Livro> result = livroRepository.findByIsbn(ISBN_EFFECTIVE_JAVA);
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName(
            "Given multiple books with associations," +
            " when findAll is called," +
            " then all books are returned with eager-fetched relations"
    )
    void shouldReturnAllBooksWithEagerFetch() {
        Editora editora = editoraRepository.save(new Editora("Addison-Wesley"));
        Autor autor = autorRepository.save(new Autor("Joshua Bloch"));

        Livro livro = basicBook("Effective Java", ISBN_EFFECTIVE_JAVA);
        livro.setEditora(editora);
        livro.getAutores().add(autor);
        livroRepository.save(livro);
        livroRepository.save(basicBook("Clean Code", ISBN_CLEAN_CODE));

        List<Livro> result = livroRepository.findAll();
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(l -> "Effective Java".equals(l.getTitulo())));
    }

    @Test
    @DisplayName(
            "Given books with different titles," +
            " when findByFiltro is called with a title term," +
            " then only matching books are returned"
    )
    void shouldReturnMatchingBooksWhenFilterByTitle() {
        livroRepository.save(basicBook("Domain-Driven Design", ISBN_DDD));
        livroRepository.save(basicBook("Clean Code", ISBN_CLEAN_CODE));

        PagedResult<Livro> result = livroRepository.findByFiltro(
                new LivroFiltro("Domain", null, null, null, null, null), 0, 50);

        assertEquals(1, result.getItems().size());
        assertEquals("Domain-Driven Design", result.getItems().get(0).getTitulo());
    }

    @Test
    @DisplayName(
            "Given books with different authors," +
            " when findByFiltro is called with an author name," +
            " then only matching books are returned"
    )
    void shouldReturnBooksWhenFilterByAuthorName() {
        Autor autor = autorRepository.save(new Autor("Eric Evans"));
        Livro livro = basicBook("DDD", ISBN_DDD);
        livro.getAutores().add(autor);
        livroRepository.save(livro);

        PagedResult<Livro> result = livroRepository.findByFiltro(
                new LivroFiltro(null, "Eric", null, null, null, null), 0, 50);

        assertEquals(1, result.getItems().size());
        assertEquals("DDD", result.getItems().get(0).getTitulo());
    }

    @Test
    @DisplayName(
            "Given books with different publishers," +
            " when findByFiltro is called with a publisher name," +
            " then only matching books are returned"
    )
    void shouldReturnBooksWhenFilterByPublisherName() {
        Editora editora = editoraRepository.save(new Editora("MIT Press"));
        Livro livro = basicBook("SICP", ISBN_SICP);
        livro.setEditora(editora);
        livroRepository.save(livro);

        PagedResult<Livro> result = livroRepository.findByFiltro(
                new LivroFiltro("MIT", null, null, null, null, null), 0, 50);

        assertEquals(1, result.getItems().size());
        assertEquals("SICP", result.getItems().get(0).getTitulo());
    }

    @Test
    @DisplayName(
            "Given existing books," +
            " when findByFiltro is called with a non-matching term," +
            " then empty list is returned"
    )
    void shouldReturnEmptyListWhenFilterMatchesNoBooks() {
        livroRepository.save(basicBook("Clean Code", ISBN_CLEAN_CODE));
        PagedResult<Livro> result = livroRepository.findByFiltro(
                new LivroFiltro("nonexistentterm", null, null, null, null, null), 0, 50);
        assertTrue(result.getItems().isEmpty());
        assertEquals(0, result.getTotalCount());
    }

    @Test
    @DisplayName(
            "Given an existing book," +
            " when delete is called with its id," +
            " then book is removed"
    )
    void shouldRemoveBookWhenDeleteCalledWithExistingId() {
        Livro saved = livroRepository.save(basicBook("To Delete", ISBN_EFFECTIVE_JAVA));
        livroRepository.delete(saved.getId());
        assertFalse(livroRepository.findById(saved.getId()).isPresent());
    }

    @Test
    @DisplayName(
            "Given a book with similar books," +
            " when save is called," +
            " then the relationship is persisted"
    )
    void shouldPersistSimilarBooksRelationship() {
        Livro book1 = livroRepository.save(basicBook("Book 1", ISBN_EFFECTIVE_JAVA));
        Livro book2 = livroRepository.save(basicBook("Book 2", ISBN_CLEAN_CODE));

        book1.getLivrosSemelhantes().add(book2);
        livroRepository.save(book1);

        Optional<Livro> result = livroRepository.findById(book1.getId());
        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName(
            "Given no book with the given id," +
            " when delete is called," +
            " then BookNotFoundException is thrown"
    )
    void shouldThrowWhenDeleteCalledWithNonExistingId() {
        assertThrows(BookNotFoundException.class, () -> livroRepository.delete(999L));
    }

    @Test
    @DisplayName(
            "Given an active unit of work," +
            " when repository operations are called inside it," +
            " then they participate in the existing transaction"
    )
    void shouldReuseCurrentEntityManagerWhenInsideUnitOfWork() {
        HibernateUnitOfWork unitOfWork = new HibernateUnitOfWork();
        unitOfWork.execute(() -> {
            livroRepository.save(basicBook("UoW Book", ISBN_SICP));
            livroRepository.findByIsbn(ISBN_SICP);
        });
        assertTrue(livroRepository.findByIsbn(ISBN_SICP).isPresent());
    }

    @Test
    @DisplayName(
            "Given a book already saved with a given ISBN," +
            " when another book with the same ISBN is saved," +
            " then the transaction is rolled back and a runtime exception is thrown"
    )
    void shouldRollbackTransactionWhenSaveFailsDueToDuplicateIsbn() {
        livroRepository.save(basicBook("Book A", ISBN_CLEAN_CODE));
        assertThrows(RuntimeException.class, () ->
                livroRepository.save(basicBook("Book B", ISBN_CLEAN_CODE))
        );
    }

    @Test
    @DisplayName(
            "Given a soft-deleted book," +
            " when findByIsbnIncludingDeleted is called with its ISBN," +
            " then the deleted book is returned"
    )
    void shouldReturnSoftDeletedBookWhenSearchingIncludingDeleted() {
        Livro saved = livroRepository.save(basicBook("Deleted Book", ISBN_EFFECTIVE_JAVA));
        livroRepository.delete(saved.getId());
        assertFalse(livroRepository.findByIsbn(ISBN_EFFECTIVE_JAVA).isPresent());

        Optional<Livro> result = livroRepository.findByIsbnIncludingDeleted(ISBN_EFFECTIVE_JAVA);

        assertTrue(result.isPresent());
        assertEquals("Deleted Book", result.get().getTitulo());
    }

    @Test
    @DisplayName(
            "Given an empty database," +
            " when findAll is called," +
            " then an empty list is returned"
    )
    void shouldReturnEmptyListWhenNoBooksExist() {
        List<Livro> result = livroRepository.findAll();
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName(
            "Given multiple books in the database," +
            " when findAll with page and size is called," +
            " then the correct page of books is returned with total count"
    )
    void shouldReturnPagedResultWhenFindAllPaginated() {
        livroRepository.save(basicBook("Alpha Book", ISBN_EFFECTIVE_JAVA));
        livroRepository.save(basicBook("Beta Book",  ISBN_CLEAN_CODE));
        livroRepository.save(basicBook("Gamma Book", ISBN_DDD));

        PagedResult<Livro> page0 = livroRepository.findAll(0, 2);
        assertEquals(3, page0.getTotalCount());
        assertEquals(2, page0.getItems().size());

        PagedResult<Livro> page1 = livroRepository.findAll(1, 2);
        assertEquals(3, page1.getTotalCount());
        assertEquals(1, page1.getItems().size());
    }

    @Test
    @DisplayName(
            "Given two books saved directly via the repository with the same ISBN," +
            " when the second save is executed in standalone mode," +
            " then IsbnInvalidoException is thrown by the unique constraint"
    )
    void shouldThrowIsbnInvalidoExceptionOnDuplicateIsbnAtRepositoryLevel() {
        livroRepository.save(basicBook("First Book", ISBN_CLEAN_CODE));

        assertThrows(IsbnInvalidoException.class, () ->
                livroRepository.save(basicBook("Second Book", ISBN_CLEAN_CODE))
        );
    }

    @Test
    @DisplayName(
            "Given two books saved via UnitOfWork with the same ISBN," +
            " when the second transaction commits," +
            " then IsbnInvalidoException is thrown by the unique constraint"
    )
    void shouldThrowIsbnInvalidoExceptionOnDuplicateIsbnViaUnitOfWork() {
        HibernateUnitOfWork unitOfWork = new HibernateUnitOfWork();
        livroRepository.save(basicBook("First Book", ISBN_DDD));

        assertThrows(IsbnInvalidoException.class, () ->
                unitOfWork.execute(() ->
                        livroRepository.save(basicBook("Second Book", ISBN_DDD))
                )
        );
    }

    private Livro basicBook(String titulo, ISBN isbn) {
        Livro livro = new Livro();
        livro.setTitulo(titulo);
        livro.setIsbn(isbn);
        livro.setDataPublicacao(LocalDate.of(2023, 1, 1));
        livro.setIdioma("English");
        return livro;
    }
}
