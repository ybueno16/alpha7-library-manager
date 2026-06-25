package br.com.yuri.alpha7.infra.persistence;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import javax.persistence.EntityManager;
import java.io.IOException;

public abstract class AbstractRepositoryTest {

    private static EmbeddedPostgres postgres;

    @BeforeAll
    static void setupDataSource() throws IOException {
        if (postgres == null) {
            postgres = EmbeddedPostgres.start();
            System.setProperty("db.url", "jdbc:postgresql://localhost:" + postgres.getPort() + "/postgres");
            System.setProperty("db.user", "postgres");
            System.setProperty("db.password", "");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try { postgres.close(); } catch (IOException ignore) {}
            }));
        }
        HibernateUtil.getEntityManagerFactory();
    }

    @AfterEach
    void cleanDatabase() {
        EntityManager em = HibernateUtil.openEntityManager();
        try {
            em.getTransaction().begin();
            em.createNativeQuery(
                    "TRUNCATE TABLE livro_semelhante, livro_autor, livro, autor, editora RESTART IDENTITY CASCADE"
            ).executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
