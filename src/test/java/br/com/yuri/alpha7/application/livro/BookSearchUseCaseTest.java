package br.com.yuri.alpha7.application.livro;

import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.repository.LivroFiltro;
import br.com.yuri.alpha7.domain.livro.repository.LivroRepository;
import br.com.yuri.alpha7.domain.livro.repository.PagedResult;
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
            "Given books in the repository," +
            " when findAll with pagination is called," +
            " then paged result is returned"
    )
    void shouldReturnPagedResultWhenFindAllWithPagination() {
        List<Livro> books = Arrays.asList(new Livro(), new Livro());
        PagedResult<Livro> pagedResult = new PagedResult<>(books, 5);
        when(livroRepository.findAll(0, 10)).thenReturn(pagedResult);

        PagedResult<Livro> result = useCase.findAll(0, 10);

        assertEquals(2, result.getItems().size());
        assertEquals(5, result.getTotalCount());
        verify(livroRepository).findAll(0, 10);
    }

    @Test
    @DisplayName(
            "Given books matching a filter term," +
            " when findByFiltro is called with pagination," +
            " then matching books are returned"
    )
    void shouldReturnFilteredBooksFromRepository() {
        List<Livro> books = Arrays.asList(new Livro());
        PagedResult<Livro> pagedResult = new PagedResult<>(books, 1);
        LivroFiltro filtro = new LivroFiltro("Clean", null, null, null, null, null);
        when(livroRepository.findByFiltro(filtro, 0, 50)).thenReturn(pagedResult);

        PagedResult<Livro> result = useCase.findByFiltro(filtro, 0, 50);

        assertEquals(1, result.getItems().size());
        verify(livroRepository).findByFiltro(filtro, 0, 50);
    }

    @Test
    @DisplayName(
            "Given no books matching the filter term," +
            " when findByFiltro is called with pagination," +
            " then empty result is returned"
    )
    void shouldReturnEmptyResultWhenFilterMatchesNoBooks() {
        PagedResult<Livro> pagedResult = new PagedResult<>(Collections.emptyList(), 0);
        LivroFiltro filtro = new LivroFiltro("nonexistent", null, null, null, null, null);
        when(livroRepository.findByFiltro(filtro, 0, 50)).thenReturn(pagedResult);

        PagedResult<Livro> result = useCase.findByFiltro(filtro, 0, 50);

        assertTrue(result.getItems().isEmpty());
        assertEquals(0, result.getTotalCount());
    }
}
