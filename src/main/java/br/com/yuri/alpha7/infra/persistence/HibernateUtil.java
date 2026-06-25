package br.com.yuri.alpha7.infra.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton responsável por inicializar e fornecer o {@link EntityManagerFactory}.
 * <p>
 * Na inicialização, executa em sequência:
 * <ol>
 *   <li>Criação do pool de conexões via HikariCP</li>
 *   <li>Migrations do banco via Flyway</li>
 *   <li>Bootstrap do Hibernate com cache L2 (Ehcache)</li>
 * </ol>
 * </p>
 */
public final class HibernateUtil {

    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);

    private static final String PERSISTENCE_UNIT    = "libraryPU";
    private static final String DEFAULT_JDBC_URL    = "jdbc:postgresql://localhost:5432/library";
    private static final String DEFAULT_DB_USER     = "library_user";
    private static final String DEFAULT_DB_PASSWORD = "library_pass";

    private static volatile HikariDataSource dataSource;
    private static volatile EntityManagerFactory entityManagerFactory;

    private HibernateUtil() {}

    /**
     * Retorna o {@link EntityManagerFactory}, inicializando-o na primeira chamada (thread-safe).
     *
     * @return instância única do {@link EntityManagerFactory}
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null) {
            synchronized (HibernateUtil.class) {
                if (entityManagerFactory == null) {
                    initialize();
                }
            }
        }
        return entityManagerFactory;
    }

    /**
     * Abre e retorna um novo {@link EntityManager}.
     * O chamador é responsável por fechar o EntityManager após o uso.
     *
     * @return novo {@link EntityManager}
     */
    public static EntityManager openEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    /**
     * Encerra o {@link EntityManagerFactory} e o pool de conexões.
     * Deve ser chamado ao encerrar a aplicação.
     */
    public static void shutdown() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        entityManagerFactory = null;
        dataSource = null;
        logger.info("HibernateUtil encerrado.");
    }

    private static void initialize() {
        dataSource = buildDataSource();
        runMigrations();
        entityManagerFactory = buildEntityManagerFactory();
        logger.info("HibernateUtil inicializado com sucesso.");
    }

    private static HikariDataSource buildDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(System.getProperty("db.url", DEFAULT_JDBC_URL));
        config.setUsername(System.getProperty("db.user", DEFAULT_DB_USER));
        config.setPassword(System.getProperty("db.password", DEFAULT_DB_PASSWORD));
        config.setPoolName("LibraryPool");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30_000);
        config.setIdleTimeout(600_000);
        config.setMaxLifetime(1_800_000);
        return new HikariDataSource(config);
    }

    private static void runMigrations() {
        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate();
        logger.info("Flyway: migrations aplicadas.");
    }

    private static EntityManagerFactory buildEntityManagerFactory() {
        try {
            Map<String, Object> props = new HashMap<>();
            props.put("hibernate.connection.datasource", dataSource);

            URL ehcacheUrl = HibernateUtil.class.getClassLoader().getResource("ehcache.xml");
            if (ehcacheUrl != null) {
                props.put("hibernate.javax.cache.uri", ehcacheUrl.toURI().toString());
            }

            return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, props);
        } catch (Exception e) {
            logger.error("Falha ao criar EntityManagerFactory.", e);
            throw new RuntimeException("Falha ao inicializar o Hibernate.", e);
        }
    }
}
