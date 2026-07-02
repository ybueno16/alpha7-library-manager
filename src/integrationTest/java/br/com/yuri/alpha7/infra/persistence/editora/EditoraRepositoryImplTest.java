package br.com.yuri.alpha7.infra.persistence.editora;

import br.com.yuri.alpha7.domain.editora.model.Editora;
import br.com.yuri.alpha7.infra.persistence.AbstractRepositoryTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import br.com.yuri.alpha7.domain.exception.LibraryException;

import static org.junit.jupiter.api.Assertions.*;

class EditoraRepositoryImplTest extends AbstractRepositoryTest {

    private final EditoraRepositoryImpl repository = new EditoraRepositoryImpl();

    @Test
    @DisplayName(
            "Given a new publisher," +
            " when save is called," +
            " then publisher is persisted with a generated id"
    )
    void shouldPersistPublisherWithGeneratedId() {
        Editora saved = repository.save(new Editora("O'Reilly"));
        assertNotNull(saved.getId());
        assertEquals("O'Reilly", saved.getNome());
    }

    @Test
    @DisplayName(
            "Given an existing publisher," +
            " when findById is called with its id," +
            " then publisher is returned"
    )
    void shouldReturnPublisherWhenFoundById() {
        Editora saved = repository.save(new Editora("Manning"));
        Optional<Editora> result = repository.findById(saved.getId());
        assertTrue(result.isPresent());
        assertEquals("Manning", result.get().getNome());
    }

    @Test
    @DisplayName(
            "Given no publisher with the given id," +
            " when findById is called," +
            " then empty optional is returned"
    )
    void shouldReturnEmptyWhenPublisherNotFoundById() {
        Optional<Editora> result = repository.findById(999L);
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName(
            "Given an existing publisher name," +
            " when findByNome is called," +
            " then publisher is returned"
    )
    void shouldReturnPublisherWhenFoundByName() {
        repository.save(new Editora("Packt"));
        Optional<Editora> result = repository.findByNome("Packt");
        assertTrue(result.isPresent());
        assertEquals("Packt", result.get().getNome());
    }

    @Test
    @DisplayName(
            "Given a non-existing publisher name," +
            " when findByNome is called," +
            " then empty optional is returned"
    )
    void shouldReturnEmptyWhenPublisherNotFoundByName() {
        Optional<Editora> result = repository.findByNome("Nonexistent");
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName(
            "Given multiple publishers exist," +
            " when findAll is called," +
            " then all publishers are returned"
    )
    void shouldReturnAllPublishers() {
        repository.save(new Editora("Editora A"));
        repository.save(new Editora("Editora B"));
        List<Editora> result = repository.findAll();
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName(
            "Given an existing publisher," +
            " when delete is called with its id," +
            " then publisher is removed"
    )
    void shouldRemovePublisherWhenDeleteCalledWithExistingId() {
        Editora saved = repository.save(new Editora("To Delete"));
        repository.delete(saved.getId());
        assertFalse(repository.findById(saved.getId()).isPresent());
    }

    @Test
    @DisplayName(
            "Given a non-existing id," +
            " when delete is called," +
            " then LibraryException is thrown"
    )
    void shouldThrowWhenDeletingNonExistentPublisher() {
        assertThrows(LibraryException.class, () -> repository.delete(999L));
    }
}
