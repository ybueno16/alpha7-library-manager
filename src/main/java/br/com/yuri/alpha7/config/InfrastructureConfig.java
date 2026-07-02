package br.com.yuri.alpha7.config;

import br.com.yuri.alpha7.application.isbn.OpenLibraryClient;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import br.com.yuri.alpha7.infra.cache.CacheManagerProvider;
import br.com.yuri.alpha7.infra.client.openlibrary.CachedBookLookup;
import br.com.yuri.alpha7.infra.client.openlibrary.CachingOpenLibraryClient;
import br.com.yuri.alpha7.infra.client.openlibrary.OpenLibraryClientImpl;
import br.com.yuri.alpha7.infra.client.openlibrary.mapper.OpenLibraryBookMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;

import javax.cache.Cache;
import java.util.concurrent.TimeUnit;

/**
 * Wiring manual da infraestrutura externa da aplicação.
 *
 * <p>Constrói o {@link OkHttpClient} usado nas chamadas HTTP, o cache JCache de consultas
 * ISBN e o {@link OpenLibraryClient} exposto à aplicação — decorado com
 * {@link br.com.yuri.alpha7.infra.client.openlibrary.CachingOpenLibraryClient} para evitar
 * requisições repetidas à API pública da OpenLibrary.
 */
public class InfrastructureConfig {

    private final OkHttpClient httpClient;
    private final OpenLibraryClient openLibraryClient;

    public InfrastructureConfig() {
        ObjectMapper objectMapper = new ObjectMapper();
        OpenLibraryBookMapper mapper = new OpenLibraryBookMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
        OpenLibraryClientImpl openLibraryClientImpl = new OpenLibraryClientImpl(
                this.httpClient,
                objectMapper,
                mapper
        );
        Cache<ISBN, CachedBookLookup> cache = CacheManagerProvider.getCacheManager()
                .getCache(
                        "isbn-api-cache",
                        ISBN.class,
                        CachedBookLookup.class
                );
        this.openLibraryClient = new CachingOpenLibraryClient(openLibraryClientImpl, cache);
    }

    /**
     * Retorna o cliente OpenLibrary decorado com cache, usado pela camada de aplicação.
     *
     * @return instância única do cliente OpenLibrary
     */
    public OpenLibraryClient openLibraryClient() {
        return openLibraryClient;
    }

    /** Libera as conexões HTTP e encerra o gerenciador de cache. */
    public void shutdown() {
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
        CacheManagerProvider.shutdown();
    }
}
