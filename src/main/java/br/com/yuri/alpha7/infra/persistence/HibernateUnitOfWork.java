package br.com.yuri.alpha7.infra.persistence;

import br.com.yuri.alpha7.application.UnitOfWork;
import br.com.yuri.alpha7.domain.exception.LibraryException;

import javax.persistence.EntityManager;
import java.util.Optional;

public class HibernateUnitOfWork implements UnitOfWork {
    private static final ThreadLocal<EntityManager> context = new ThreadLocal<>();
    @Override
    public void execute(Runnable action) {
        EntityManager entityManager = HibernateUtil.openEntityManager();
        context.set(entityManager);
        try {
            entityManager.getTransaction().begin();
            action.run();
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new LibraryException("Transaction rolled back: " + e.getMessage());
        } finally {
            context.remove();
            entityManager.close();
        }
    }

    public static Optional<EntityManager> getCurrentEntityManager() {
        return Optional.ofNullable(context.get());
    }
}
