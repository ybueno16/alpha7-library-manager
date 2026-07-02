package br.com.yuri.alpha7.infra.persistence;

import br.com.yuri.alpha7.domain.exception.IsbnInvalidoException;
import br.com.yuri.alpha7.domain.exception.LibraryException;
import org.hibernate.exception.ConstraintViolationException;

import javax.persistence.EntityManager;
import java.util.Optional;
import java.util.function.Function;

/**
 * Classe base para repositórios Hibernate que oferece dois modos de execução:
 * com e sem {@link HibernateUnitOfWork} ativo na thread corrente.
 *
 * <p><strong>Modo Unit of Work</strong> — quando {@code ImportUseCase} (ou qualquer outro
 * orquestrador) chama {@link HibernateUnitOfWork#execute(Runnable)}, o {@code EntityManager}
 * da transação externa é armazenado em um {@link ThreadLocal}. Os métodos deste repositório
 * detectam a presença desse contexto e reutilizam o mesmo {@code EntityManager}, participando
 * da transação já aberta sem criar uma nova.
 *
 * <p><strong>Modo autônomo</strong> — na ausência de um Unit of Work ativo (ex: operações
 * disparadas diretamente pela UI), cada chamada abre seu próprio {@code EntityManager},
 * gerencia a transação e fecha a conexão ao final. Isso garante que leituras e escritas simples
 * funcionem corretamente mesmo fora de um contexto transacional explícito.
 *
 * <p>O método {@link #executeInTransaction} faz commit ou rollback automaticamente no modo
 * autônomo. O método {@link #executeQuery} não abre transação — apenas abre e fecha o
 * {@code EntityManager} — pois leituras não precisam de demarcação transacional explícita no
 * Hibernate com auto-flush desabilitado.
 */
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
        } catch (LibraryException e) {
            entityManager.getTransaction().rollback();
            throw e;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            ConstraintViolationException cve = HibernateUnitOfWork.unwrapConstraintViolation(e);
            if (cve != null) {
                if (cve.getConstraintName() != null && cve.getConstraintName().toLowerCase().contains("isbn")) {
                    throw new IsbnInvalidoException("ISBN já cadastrado no acervo");
                }
                throw new LibraryException("Violação de regra de integridade dos dados", cve);
            }
            throw new LibraryException("Não foi possível completar a operação", e);
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
