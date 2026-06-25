package br.com.yuri.alpha7.infra.persistence;

import br.com.yuri.alpha7.domain.exception.LibraryException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class HibernateUnitOfWorkTest extends AbstractRepositoryTest {

    private final HibernateUnitOfWork unitOfWork = new HibernateUnitOfWork();

    @Test
    @DisplayName(
            "Given a successful action," +
            " when execute is called," +
            " then the action runs and transaction is committed"
    )
    void shouldCommitTransactionWhenActionSucceeds() {
        assertDoesNotThrow(() -> unitOfWork.execute(() -> {}));
    }

    @Test
    @DisplayName(
            "Given an action that throws," +
            " when execute is called," +
            " then transaction is rolled back and LibraryException is thrown"
    )
    void shouldRollbackAndThrowLibraryExceptionWhenActionFails() {
        assertThrows(LibraryException.class, () ->
                unitOfWork.execute(() -> { throw new RuntimeException("simulated failure"); })
        );
    }

    @Test
    @DisplayName(
            "Given no active transaction," +
            " when getCurrentEntityManager is called," +
            " then empty optional is returned"
    )
    void shouldReturnEmptyWhenNoActiveTransaction() {
        assertFalse(HibernateUnitOfWork.getCurrentEntityManager().isPresent());
    }

    @Test
    @DisplayName(
            "Given an active transaction," +
            " when getCurrentEntityManager is called inside execute," +
            " then the entity manager is present"
    )
    void shouldReturnEntityManagerDuringExecution() {
        AtomicReference<Optional<EntityManager>> captured = new AtomicReference<>();

        unitOfWork.execute(() ->
                captured.set(HibernateUnitOfWork.getCurrentEntityManager())
        );

        assertTrue(captured.get().isPresent());
    }
}
