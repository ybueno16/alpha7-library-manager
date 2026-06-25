package br.com.yuri.alpha7.application.livro;

import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.repository.LivroRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookSearchUseCaseTest {

    @Mock
    private LivroRepository livroRepository;

    @InjectMocks
    private BookSearchUseCase useCase;

    @Test
    @DisplayName(
            "Given books in the repository," +
            " when findAll is called," +
            " then all books are returned"
    )
    void shouldReturnAllBooksFromRepository() {
        List<Livro> books = Arrays.asList(new Livro(), new Livro());
        when(livroRepository.findAll()).thenReturn(books);

        List<Livro> result = useCase.findAll();

        assertEquals(2, result.size());
        verify(livroRepository).findAll();
    }

    @Test
    @DisplayName(
            "Given no books in the repository," +
            " when findAll is called," +
            " then empty list is returned"
    )
    void shouldReturnEmptyListWhenNoBooksExist() {
        when(livroRepository.findAll()).thenReturn(Collections.emptyList());

        List<Livro> result = useCase.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName(
            "Given books matching a filter term," +
            " when findByFiltro is called," +
            " then matching books are returned"
    )
    void shouldReturnFilteredBooksFromRepository() {
        List<Livro> books = Arrays.asList(new Livro());
        when(livroRepository.findByFiltro("Clean")).thenReturn(books);

        List<Livro> result = useCase.findByFiltro("Clean");

        assertEquals(1, result.size());
        verify(livroRepository).findByFiltro("Clean");
    }

    @Test
    @DisplayName(
            "Given no books matching the filter term," +
            " when findByFiltro is called," +
            " then empty list is returned"
    )
    void shouldReturnEmptyListWhenFilterMatchesNoBooks() {
        when(livroRepository.findByFiltro("nonexistent")).thenReturn(Collections.emptyList());

        List<Livro> result = useCase.findByFiltro("nonexistent");

        assertTrue(result.isEmpty());
    }
}
