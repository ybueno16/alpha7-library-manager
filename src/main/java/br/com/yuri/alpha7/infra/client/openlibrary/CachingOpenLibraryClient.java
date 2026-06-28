package br.com.yuri.alpha7.infra.client.openlibrary;

import br.com.yuri.alpha7.application.isbn.OpenLibraryClient;
import br.com.yuri.alpha7.domain.exception.OpenLibraryUnavailableException;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import java.util.Optional;

/**
 * Decorator que adiciona cache de resultados positivos ao {@link OpenLibraryClient} delegado.
 *
 * <p>O cache armazena apenas livros encontrados ({@link CachedBookLookup#found(br.com.yuri.alpha7.domain.livro.model.Livro)}).
 * ISBNs que retornam {@code Optional.empty()} (livro não encontrado na OpenLibrary) nunca são
 * inseridos no cache — uma próxima consulta ao mesmo ISBN sempre tentará a API de novo, pois o
 * livro pode ter sido adicionado ao catálogo da OpenLibrary desde a última consulta.
 *
 * <p>Quando o serviço externo lança {@link br.com.yuri.alpha7.domain.exception.OpenLibraryUnavailableException},
 * a exceção é relançada sem alterar o cache. Isso garante que uma indisponibilidade temporária
 * não "envenene" o cache com uma ausência forçada.
 *
 * <p>O cache utilizado é o {@code isbn-api-cache} configurado em {@code ehcache-isbn-cache.xml},
 * separado do cache L2 do Hibernate que armazena entidades JPA.
 */
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
            logger.debug("Cache hit para ISBN {}", isbn.getValue());
            return cached.toOptional();
        }

        logger.debug("Cache miss para ISBN {} — consultando OpenLibrary", isbn.getValue());
        try {
            Optional<Livro> result = delegate.findByIsbn(isbn);
            result.ifPresent(livro -> {
                logger.debug("Resultado em cache para ISBN {}: '{}'", isbn.getValue(), livro.getTitulo());
                cache.put(isbn, CachedBookLookup.found(livro));
            });
            return result;
        } catch (OpenLibraryUnavailableException e) {
            logger.warn("OpenLibrary indisponível para ISBN {}: {}", isbn.getValue(), e.getMessage());
            throw e;
        }
    }
}
