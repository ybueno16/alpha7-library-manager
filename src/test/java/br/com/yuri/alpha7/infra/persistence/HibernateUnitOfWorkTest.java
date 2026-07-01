package br.com.yuri.alpha7.infra.persistence;

import br.com.yuri.alpha7.domain.exception.LibraryException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

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
                unitOfWork.execute((Runnable) () -> { throw new RuntimeException("simulated failure"); })
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
            "Given an action that throws a LibraryException," +
            " when execute is called," +
            " then transaction is rolled back and the same exception is re-thrown"
    )
    void shouldRollbackAndRethrowOriginalLibraryException() {
        LibraryException original = new LibraryException("domain constraint violated");
        LibraryException thrown = assertThrows(LibraryException.class, () ->
                unitOfWork.execute((Runnable) () -> { throw original; })
        );
        assertSame(original, thrown);
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

    @Test
    @DisplayName(
            "Given a supplier that returns a value," +
            " when execute with Supplier is called," +
            " then the returned value is propagated and transaction is committed"
    )
    void shouldReturnSupplierResultAndCommitTransaction() {
        String result = unitOfWork.execute(() -> "expected");
        assertEquals("expected", result);
    }

    @Test
    @DisplayName(
            "Given a supplier that throws a RuntimeException," +
            " when execute with Supplier is called," +
            " then transaction is rolled back and LibraryException is thrown"
    )
    void shouldRollbackAndWrapExceptionFromSupplier() {
        assertThrows(LibraryException.class, () ->
                unitOfWork.execute((Supplier<String>) () -> {
                    throw new RuntimeException("supplier failure");
                })
        );
    }

    @Test
    @DisplayName(
            "Given a supplier that throws a LibraryException," +
            " when execute with Supplier is called," +
            " then transaction is rolled back and the same exception is re-thrown"
    )
    void shouldRollbackAndRethrowLibraryExceptionFromSupplier() {
        LibraryException original = new LibraryException("supplier domain error");
        LibraryException thrown = assertThrows(LibraryException.class, () ->
                unitOfWork.execute((Supplier<String>) () -> { throw original; })
        );
        assertSame(original, thrown);
    }
}
