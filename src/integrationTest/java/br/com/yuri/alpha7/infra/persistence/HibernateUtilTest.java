package br.com.yuri.alpha7.infra.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HibernateUtilTest extends AbstractRepositoryTest {

    @Test
    @DisplayName(
            "Given an initialized HibernateUtil," +
            " when shutdown is called," +
            " then it closes without throwing and can be reinitialized"
    )
    void shouldCloseEntityManagerFactoryAndDataSourceOnShutdown() {
        HibernateUtil.getEntityManagerFactory();
        assertDoesNotThrow(HibernateUtil::shutdown);
        assertNotNull(HibernateUtil.getEntityManagerFactory());
    }

    @Test
    @DisplayName(
            "Given HibernateUtil with null factory and datasource," +
            " when shutdown is called," +
            " then no exception is thrown"
    )
    void shouldNoopWhenShutdownCalledWithNullState() {
        HibernateUtil.shutdown();
        assertDoesNotThrow(HibernateUtil::shutdown);
        assertNotNull(HibernateUtil.getEntityManagerFactory());
    }
}
