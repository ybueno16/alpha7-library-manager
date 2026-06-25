package br.com.yuri.alpha7.infra.cache;

import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import br.com.yuri.alpha7.infra.client.openlibrary.CachedBookLookup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.*;

class CacheManagerProviderTest {

    @AfterEach
    void shutdownAfterEachTest() {
        CacheManagerProvider.shutdown();
    }

    @Test
    @DisplayName(
            "Given the ehcache-isbn-cache.xml on the classpath," +
            " when getCacheManager is called," +
            " then it returns an open CacheManager"
    )
    void shouldReturnOpenCacheManager() {
        CacheManager cacheManager = CacheManagerProvider.getCacheManager();

        assertNotNull(cacheManager);
        assertFalse(cacheManager.isClosed());
    }

    @Test
    @DisplayName(
            "Given getCacheManager was already called once," +
            " when it is called again," +
            " then the same instance is returned"
    )
    void shouldReturnSameInstanceOnRepeatedCalls() {
        CacheManager first = CacheManagerProvider.getCacheManager();
        CacheManager second = CacheManagerProvider.getCacheManager();

        assertSame(first, second);
    }

    @Test
    @DisplayName(
            "Given the isbn-api-cache region configured in ehcache-isbn-cache.xml," +
            " when getCacheManager is called," +
            " then the configured cache region can be retrieved"
    )
    void shouldExposeIsbnApiCacheRegion() {
        CacheManager cacheManager = CacheManagerProvider.getCacheManager();

        assertNotNull(cacheManager.getCache("isbn-api-cache", ISBN.class, CachedBookLookup.class));
    }

    @Test
    @DisplayName(
            "Given an open CacheManager," +
            " when shutdown is called," +
            " then the CacheManager is closed"
    )
    void shouldCloseCacheManagerOnShutdown() {
        CacheManager cacheManager = CacheManagerProvider.getCacheManager();

        CacheManagerProvider.shutdown();

        assertTrue(cacheManager.isClosed());
    }

    @Test
    @DisplayName(
            "Given a CacheManager that was already closed externally," +
            " when shutdown is called," +
            " then no exception is thrown"
    )
    void shouldNoopWhenCacheManagerIsAlreadyClosed() {
        CacheManager cm = CacheManagerProvider.getCacheManager();
        cm.close();
        assertDoesNotThrow(() -> CacheManagerProvider.shutdown());
    }
}
