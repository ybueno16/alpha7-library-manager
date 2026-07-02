package br.com.yuri.alpha7.application.isbn;

import br.com.yuri.alpha7.domain.livro.model.Livro;
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
    private OpenLibraryClient openLibraryClient;

    @InjectMocks
    private IsbnLookupUseCase useCase;

    private static final ISBN VALID_ISBN = new ISBN("9780132350884");

    @Test
    @DisplayName(
            "Given a valid ISBN," +
            " when findByIsbn is called," +
            " then OpenLibrary is queried directly (cache or API)"
    )
    void shouldDelegateToOpenLibraryDirectly() {
        Livro livro = new Livro();
        livro.setIsbn(VALID_ISBN);
        when(openLibraryClient.findByIsbn(VALID_ISBN)).thenReturn(Optional.of(livro));

        Optional<Livro> result = useCase.findByIsbn(VALID_ISBN);

        assertTrue(result.isPresent());
        assertEquals(VALID_ISBN, result.get().getIsbn());
        verify(openLibraryClient).findByIsbn(VALID_ISBN);
    }

    @Test
    @DisplayName(
            "Given a valid ISBN not found in OpenLibrary," +
            " when findByIsbn is called," +
            " then empty optional is returned"
    )
    void shouldReturnEmptyWhenNotFoundInOpenLibrary() {
        when(openLibraryClient.findByIsbn(VALID_ISBN)).thenReturn(Optional.empty());

        Optional<Livro> result = useCase.findByIsbn(VALID_ISBN);

        assertFalse(result.isPresent());
    }
}
