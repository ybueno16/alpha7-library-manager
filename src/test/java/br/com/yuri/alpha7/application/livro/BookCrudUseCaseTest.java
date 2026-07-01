package br.com.yuri.alpha7.application.livro;

import br.com.yuri.alpha7.application.UnitOfWork;
import br.com.yuri.alpha7.domain.editora.repository.EditoraRepository;
import br.com.yuri.alpha7.domain.exception.BookNotFoundException;
import br.com.yuri.alpha7.domain.exception.IsbnInvalidoException;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.repository.LivroRepository;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookCrudUseCaseTest {

    @Mock
    private LivroRepository  livroRepository;
    @Mock
    private EditoraRepository editoraRepository;
    @Mock
    private UnitOfWork        unitOfWork;

    @InjectMocks
    private BookCrudUseCase useCase;

    private static final ISBN VALID_ISBN = new ISBN("9780132350884");

    @Test
    @DisplayName(
            "Given a book with a new ISBN," +
            " when save is called," +
            " then book is persisted via repository"
    )
    void shouldPersistBookWhenIsbnIsNew() {
        Livro livro = bookWithIsbn(VALID_ISBN);
        when(livroRepository.findByIsbn(VALID_ISBN)).thenReturn(Optional.empty());
        when(livroRepository.save(livro)).thenReturn(livro);

        Livro result = useCase.save(livro);

        assertNotNull(result);
        verify(livroRepository).save(livro);
    }

    @Test
    @DisplayName(
            "Given a book with a duplicate ISBN," +
            " when save is called," +
            " then IsbnInvalidoException is thrown"
    )
    void shouldThrowExceptionWhenIsbnIsDuplicated() {
        Livro livro = bookWithIsbn(VALID_ISBN);
        when(livroRepository.findByIsbn(VALID_ISBN)).thenReturn(Optional.of(livro));

        assertThrows(IsbnInvalidoException.class, () -> useCase.save(livro));
        verify(livroRepository, never()).save(any());
    }

    @Test
    @DisplayName(
            "Given an existing book," +
            " when findById is called with its id," +
            " then book is returned"
    )
    void shouldReturnBookWhenFoundById() {
        Livro livro = bookWithIsbn(VALID_ISBN);
        when(livroRepository.findById(1L)).thenReturn(Optional.of(livro));

        Optional<Livro> result = useCase.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(livro, result.get());
    }

    @Test
    @DisplayName(
            "Given no book with the given id," +
            " when findById is called," +
            " then BookNotFoundException is thrown"
    )
    void shouldThrowBookNotFoundExceptionWhenIdDoesNotExist() {
        when(livroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> useCase.findById(99L));
    }

    @Test
    @DisplayName(
            "Given an existing book," +
            " when delete is called," +
            " then deletion is delegated to the repository"
    )
    void shouldDelegateDeleteToRepositoryWhenBookExists() {
        when(livroRepository.findById(1L)).thenReturn(Optional.of(new Livro()));

        assertDoesNotThrow(() -> useCase.delete(1L));
        verify(livroRepository).delete(1L);
    }

    @Test
    @DisplayName(
            "Given no book with the given id," +
            " when delete is called," +
            " then BookNotFoundException is thrown"
    )
    void shouldThrowBookNotFoundExceptionWhenDeletingNonExistentBook() {
        when(livroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> useCase.delete(99L));
        verify(livroRepository, never()).delete(anyLong());
    }

    private Livro bookWithIsbn(ISBN isbn) {
        Livro livro = new Livro();
        livro.setTitulo("Clean Code");
        livro.setIsbn(isbn);
        return livro;
    }
}
