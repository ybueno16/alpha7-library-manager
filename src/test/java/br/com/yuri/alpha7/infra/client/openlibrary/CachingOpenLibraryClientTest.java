package br.com.yuri.alpha7.infra.client.openlibrary;

import br.com.yuri.alpha7.application.isbn.OpenLibraryClient;
import br.com.yuri.alpha7.domain.exception.OpenLibraryUnavailableException;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.cache.Cache;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CachingOpenLibraryClientTest {

    @Mock
    private OpenLibraryClient delegate;

    @Mock
    private Cache<ISBN, CachedBookLookup> cache;

    private CachingOpenLibraryClient client;

    private static final ISBN VALID_ISBN = new ISBN("9780132350884");

    @BeforeEach
    void setUp() {
        client = new CachingOpenLibraryClient(delegate, cache);
    }

    @Test
    @DisplayName(
            "Given a cached result for an ISBN," +
            " when findByIsbn is called," +
            " then the cached result is returned without calling the delegate"
    )
    void shouldReturnCachedResultWhenCacheHit() {
        Livro livro = new Livro();
        when(cache.get(VALID_ISBN)).thenReturn(CachedBookLookup.found(livro));

        Optional<Livro> result = client.findByIsbn(VALID_ISBN);

        assertTrue(result.isPresent());
        assertEquals(livro, result.get());
        verify(delegate, never()).findByIsbn(any());
    }

    @Test
    @DisplayName(
            "Given no cached result and the delegate finds a book," +
            " when findByIsbn is called," +
            " then the book is cached and returned"
    )
    void shouldFetchFromDelegateAndCacheBookWhenCacheMiss() {
        Livro livro = new Livro();
        when(cache.get(VALID_ISBN)).thenReturn(null);
        when(delegate.findByIsbn(VALID_ISBN)).thenReturn(Optional.of(livro));

        Optional<Livro> result = client.findByIsbn(VALID_ISBN);

        assertTrue(result.isPresent());
        assertEquals(livro, result.get());

        ArgumentCaptor<CachedBookLookup> captor = ArgumentCaptor.forClass(CachedBookLookup.class);
        verify(cache).put(eq(VALID_ISBN), captor.capture());
        assertEquals(Optional.of(livro), captor.getValue().toOptional());
    }

    @Test
    @DisplayName(
            "Given no cached result and the delegate returns empty," +
            " when findByIsbn is called," +
            " then empty is returned without caching"
    )
    void shouldNotCacheWhenDelegateReturnsEmpty() {
        when(cache.get(VALID_ISBN)).thenReturn(null);
        when(delegate.findByIsbn(VALID_ISBN)).thenReturn(Optional.empty());

        Optional<Livro> result = client.findByIsbn(VALID_ISBN);

        assertFalse(result.isPresent());
        verify(cache, never()).put(any(), any());
    }

    @Test
    @DisplayName(
            "Given no cached result and the delegate throws OpenLibraryUnavailableException," +
            " when findByIsbn is called," +
            " then the exception is propagated without caching"
    )
    void shouldPropagateExceptionAndNotCacheWhenDelegateThrows() {
        when(cache.get(VALID_ISBN)).thenReturn(null);
        when(delegate.findByIsbn(VALID_ISBN)).thenThrow(new OpenLibraryUnavailableException("timeout"));

        assertThrows(OpenLibraryUnavailableException.class, () -> client.findByIsbn(VALID_ISBN));
        verify(cache, never()).put(any(), any());
    }
}
