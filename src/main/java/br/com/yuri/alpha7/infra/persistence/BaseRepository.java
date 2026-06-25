package br.com.yuri.alpha7.infra.persistence;

import javax.persistence.EntityManager;
import java.util.Optional;
import java.util.function.Function;

public abstract class BaseRepository {
    protected <T> T executeInTransaction(Function<EntityManager, T> action){
        Optional<EntityManager> currentEm = HibernateUnitOfWork.getCurrentEntityManager();
        if (currentEm.isPresent()) {
            return action.apply(currentEm.get());
        }

        EntityManager entityManager = HibernateUtil.openEntityManager();
        try {
            entityManager.getTransaction().begin();
            T result = action.apply(entityManager);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e){
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }
    protected <T> T executeQuery(Function<EntityManager, T> action) {
        Optional<EntityManager> currentEm = HibernateUnitOfWork.getCurrentEntityManager();
        if (currentEm.isPresent()) {
            return action.apply(currentEm.get());
        }

        EntityManager entityManager = HibernateUtil.openEntityManager();
        try {
            return action.apply(entityManager);
        } finally {
            entityManager.close();
        }
    }
}
