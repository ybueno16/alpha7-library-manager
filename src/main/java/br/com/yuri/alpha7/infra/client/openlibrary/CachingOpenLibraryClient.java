package br.com.yuri.alpha7.infra.client.openlibrary;

import br.com.yuri.alpha7.application.isbn.OpenLibraryClient;
import br.com.yuri.alpha7.domain.exception.OpenLibraryUnavailableException;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import java.util.Optional;

public class CachingOpenLibraryClient implements OpenLibraryClient {
    private static final Logger logger = LoggerFactory.getLogger(CachingOpenLibraryClient.class);
    private final OpenLibraryClient delegate;
    private final Cache<ISBN,CachedBookLookup> cache;

    public CachingOpenLibraryClient(OpenLibraryClient delegate, Cache<ISBN, CachedBookLookup> cache) {
        this.delegate = delegate;
        this.cache = cache;
    }

    @Override
    public Optional<Livro> findByIsbn(ISBN isbn) {
        CachedBookLookup cached = cache.get(isbn);
        if (cached != null) {
            return cached.toOptional();
        }
        try {
            Optional<Livro> result = delegate.findByIsbn(isbn);
            result.ifPresent(livro -> cache.put(isbn, CachedBookLookup.found(livro)));
            return result;
        } catch (OpenLibraryUnavailableException e) {
            logger.warn("OpenLibrary unavailable for ISBN {}: {}", isbn.getValue(), e.getMessage());
            throw e;
        }
    }
}
