package br.com.yuri.alpha7.infra.persistence.autor;

import br.com.yuri.alpha7.domain.autor.model.Autor;
import br.com.yuri.alpha7.infra.persistence.AbstractRepositoryTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AutorRepositoryImplTest extends AbstractRepositoryTest {

    private final AutorRepositoryImpl repository = new AutorRepositoryImpl();

    @Test
    @DisplayName(
            "Given a new author," +
            " when save is called," +
            " then author is persisted with a generated id"
    )
    void shouldPersistAuthorWithGeneratedId() {
        Autor autor = new Autor("Joshua Bloch");
        autor.setDataNascimento(LocalDate.of(1961, 8, 28));
        Autor saved = repository.save(autor);
        assertNotNull(saved.getId());
        assertEquals("Joshua Bloch", saved.getNome());
    }

    @Test
    @DisplayName(
            "Given an existing author," +
            " when findById is called with its id," +
            " then author is returned"
    )
    void shouldReturnAuthorWhenFoundById() {
        Autor saved = repository.save(new Autor("Robert Martin"));
        Optional<Autor> result = repository.findById(saved.getId());
        assertTrue(result.isPresent());
        assertEquals("Robert Martin", result.get().getNome());
    }

    @Test
    @DisplayName(
            "Given no author with the given id," +
            " when findById is called," +
            " then empty optional is returned"
    )
    void shouldReturnEmptyWhenAuthorNotFoundById() {
        Optional<Autor> result = repository.findById(999L);
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName(
            "Given an existing author name," +
            " when findByNome is called," +
            " then author is returned"
    )
    void shouldReturnAuthorWhenFoundByName() {
        repository.save(new Autor("Martin Fowler"));
        Optional<Autor> result = repository.findByNome("Martin Fowler");
        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName(
            "Given a non-existing author name," +
            " when findByNome is called," +
            " then empty optional is returned"
    )
    void shouldReturnEmptyWhenAuthorNotFoundByName() {
        Optional<Autor> result = repository.findByNome("Nonexistent");
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName(
            "Given multiple authors exist," +
            " when findAll is called," +
            " then all authors are returned"
    )
    void shouldReturnAllAuthors() {
        repository.save(new Autor("Autor A"));
        repository.save(new Autor("Autor B"));
        repository.save(new Autor("Autor C"));
        List<Autor> result = repository.findAll();
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName(
            "Given an existing author," +
            " when delete is called with its id," +
            " then author is removed"
    )
    void shouldRemoveAuthorWhenDeleteCalledWithExistingId() {
        Autor saved = repository.save(new Autor("To Delete"));
        repository.delete(saved.getId());
        assertFalse(repository.findById(saved.getId()).isPresent());
    }

    @Test
    @DisplayName(
            "Given no author with the given id," +
            " when delete is called," +
            " then no exception is thrown"
    )
    void shouldNoopWhenDeleteCalledWithNonExistingId() {
        assertDoesNotThrow(() -> repository.delete(999L));
    }

    @Test
    @DisplayName(
            "Given an author with optional fields," +
            " when save is called," +
            " then all fields are persisted correctly"
    )
    void shouldPersistAuthorWithOptionalFields() {
        Autor autor = new Autor("Edsger Dijkstra");
        autor.setDataNascimento(LocalDate.of(1930, 5, 11));
        autor.setDataFalecimento(LocalDate.of(2002, 8, 6));
        autor.setBio("Pioneer of computer science.");
        Autor saved = repository.save(autor);
        assertEquals("Pioneer of computer science.", saved.getBio());
        assertEquals(LocalDate.of(2002, 8, 6), saved.getDataFalecimento());
        assertEquals(LocalDate.of(1930, 5, 11), saved.getDataNascimento());
    }
}
