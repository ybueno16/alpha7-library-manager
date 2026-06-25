package br.com.yuri.alpha7.infra.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.net.URI;
import java.net.URL;

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

    public static void shutdown() {
        if (cacheManager != null && !cacheManager.isClosed()) {
            cacheManager.close();
        }
        cacheManager = null;
        logger.info("CacheManagerProvider encerrado.");
    }

    private static CacheManager initialize() {
        try {
            ClassLoader classLoader = CacheManagerProvider.class.getClassLoader();
            URL configUrl = classLoader.getResource(CONFIG_FILE);
            if (configUrl == null) {
                throw new IllegalStateException("Arquivo de configuração não encontrado no classpath: " + CONFIG_FILE);
            }

            URI configUri = configUrl.toURI();
            CachingProvider cachingProvider = Caching.getCachingProvider();
            CacheManager manager = cachingProvider.getCacheManager(configUri, classLoader);

            logger.info("CacheManagerProvider inicializado com sucesso.");
            return manager;
        } catch (Exception e) {
            logger.error("Falha ao inicializar o CacheManagerProvider.", e);
            throw new RuntimeException("Falha ao inicializar o CacheManagerProvider.", e);
        }
    }
}
