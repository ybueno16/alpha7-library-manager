package br.com.yuri.alpha7.infra.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.net.URI;
import java.net.URL;

/**
 * Singleton que inicializa e fornece o {@link CacheManager} da JCache (JSR-107) usado para
 * o cache de consultas ISBN da OpenLibrary.
 *
 * <p>Este provider gerencia exclusivamente o cache de ISBN ({@code isbn-api-cache}), configurado
 * em {@code ehcache-isbn-cache.xml}. Ele é distinto do cache L2 do Hibernate — que armazena
 * entidades JPA e é configurado em {@code ehcache.xml} e inicializado por {@link br.com.yuri.alpha7.infra.persistence.HibernateUtil}.
 *
 * <p>A inicialização é thread-safe via double-checked locking com campo {@code volatile}.
 * O método {@link #shutdown()} deve ser chamado ao encerrar a aplicação (veja
 * {@link br.com.yuri.alpha7.config.InfrastructureConfig#shutdown()}) para fechar conexões e
 * liberar recursos do provider Ehcache.
 */
public class CacheManagerProvider {

    private static final Logger logger = LoggerFactory.getLogger(CacheManagerProvider.class);
    private static final String CONFIG_FILE = "ehcache-isbn-cache.xml";

    private static volatile CacheManager cacheManager;

    private CacheManagerProvider() {
    }

    public static CacheManager getCacheManager() {
        if (cacheManager == null) {
            synchronized (CacheManagerProvider.class) {
                if (cacheManager == null) {
                    cacheManager = initialize();
                }
            }
        }
        return cacheManager;
    }

    public static synchronized void shutdown() {
        if (cacheManager != null && !cacheManager.isClosed()) {
            cacheManager.close();
        }
        cacheManager = null;
        logger.info("Cache de ISBN encerrado");
    }

    private static CacheManager initialize() {
        try {
            ClassLoader classLoader = CacheManagerProvider.class.getClassLoader();
            URL configUrl = classLoader.getResource(CONFIG_FILE);
            if (configUrl == null) {
                throw new IllegalStateException("Arquivo de configuração não encontrado no classpath: " + CONFIG_FILE);
            }

            logger.debug("Carregando configuração de cache ISBN: {}", configUrl);
            URI configUri = configUrl.toURI();
            CachingProvider cachingProvider = Caching.getCachingProvider();
            CacheManager manager = cachingProvider.getCacheManager(configUri, classLoader);

            logger.info("Cache de ISBN inicializado via '{}'", CONFIG_FILE);
            return manager;
        } catch (Exception e) {
            logger.error("Falha ao inicializar cache de ISBN a partir de '{}': {}", CONFIG_FILE, e.getMessage(), e);
            throw new RuntimeException("Falha ao inicializar o CacheManagerProvider.", e);
        }
    }
}
