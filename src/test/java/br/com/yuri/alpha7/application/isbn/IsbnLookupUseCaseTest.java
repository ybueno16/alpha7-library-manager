package br.com.yuri.alpha7.application.isbn;

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
class IsbnLookupUseCaseTest {

    @Mock
    private LivroRepository livroRepository;

    @Mock
    private OpenLibraryClient openLibraryClient;

    @InjectMocks
    private IsbnLookupUseCase useCase;

    private static final ISBN VALID_ISBN = new ISBN("9780132350884");

    @Test
    @DisplayName(
            "Given a book registered with a given ISBN," +
            " when findByIsbn is called," +
            " then book is returned from local repository without calling OpenLibrary"
    )
    void shouldReturnBookFromLocalRepositoryWhenFound() {
        Livro livro = new Livro();
        livro.setIsbn(VALID_ISBN);
        when(livroRepository.findByIsbn(VALID_ISBN)).thenReturn(Optional.of(livro));

        Optional<Livro> result = useCase.findByIsbn(VALID_ISBN);

        assertTrue(result.isPresent());
        assertEquals(VALID_ISBN, result.get().getIsbn());
        verify(livroRepository).findByIsbn(VALID_ISBN);
        verify(openLibraryClient, never()).findByIsbn(any());
    }

    @Test
    @DisplayName(
            "Given no book in the local repository for the given ISBN," +
            " when findByIsbn is called," +
            " then OpenLibrary is queried as fallback"
    )
    void shouldDelegateToOpenLibraryWhenNotFoundLocally() {
        Livro livro = new Livro();
        livro.setIsbn(VALID_ISBN);
        when(livroRepository.findByIsbn(VALID_ISBN)).thenReturn(Optional.empty());
        when(openLibraryClient.findByIsbn(VALID_ISBN)).thenReturn(Optional.of(livro));

        Optional<Livro> result = useCase.findByIsbn(VALID_ISBN);

        assertTrue(result.isPresent());
        verify(openLibraryClient).findByIsbn(VALID_ISBN);
    }

    @Test
    @DisplayName(
            "Given no book in the local repository and OpenLibrary returns empty," +
            " when findByIsbn is called," +
            " then empty optional is returned"
    )
    void shouldReturnEmptyWhenNotFoundLocallyOrInOpenLibrary() {
        when(livroRepository.findByIsbn(VALID_ISBN)).thenReturn(Optional.empty());
        when(openLibraryClient.findByIsbn(VALID_ISBN)).thenReturn(Optional.empty());

        Optional<Livro> result = useCase.findByIsbn(VALID_ISBN);

        assertFalse(result.isPresent());
    }
}
