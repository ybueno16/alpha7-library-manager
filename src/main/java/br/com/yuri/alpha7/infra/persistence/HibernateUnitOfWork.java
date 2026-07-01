package br.com.yuri.alpha7.infra.persistence;

import br.com.yuri.alpha7.application.UnitOfWork;
import br.com.yuri.alpha7.domain.exception.LibraryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Implementação de {@link UnitOfWork} baseada em Hibernate que demarca transações e
 * propaga o {@link EntityManager} ativo para os repositórios via {@link ThreadLocal}.
 *
 * <p>O fluxo de uma execução é:
 * <ol>
 *   <li>Abre um novo {@code EntityManager} e armazena-o no {@link ThreadLocal}.</li>
 *   <li>Inicia a transação e executa a {@code Runnable} fornecida.</li>
 *   <li>Os repositórios chamados dentro da {@code Runnable} detectam o contexto via
 *       {@link #getCurrentEntityManager()} e reutilizam o mesmo {@code EntityManager},
 *       participando da transação sem criar conexões adicionais.</li>
 *   <li>Ao término da {@code Runnable}: commit em caso de sucesso, rollback em caso de
 *       exceção, e remoção do contexto do {@link ThreadLocal} em ambos os casos.</li>
 * </ol>
 *
 * <p>Isso é especialmente importante na importação de CSV, onde cada linha deve ser
 * processada em uma transação independente mas os repositórios precisam compartilhar
 * a mesma sessão Hibernate dentro de cada linha.
 */
public class HibernateUnitOfWork implements UnitOfWork {

    private static final Logger logger = LoggerFactory.getLogger(HibernateUnitOfWork.class);
    private static final ThreadLocal<EntityManager> context = new ThreadLocal<>();

    @Override
    public void execute(Runnable action) {
        EntityManager entityManager = HibernateUtil.openEntityManager();
        context.set(entityManager);
        try {
            logger.debug("Iniciando transação");
            entityManager.getTransaction().begin();
            action.run();
            entityManager.getTransaction().commit();
            logger.debug("Transação confirmada");
        } catch (LibraryException e) {
            entityManager.getTransaction().rollback();
            logger.warn("Transação revertida: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            logger.warn("Transação revertida: {}", e.getMessage());
            throw new LibraryException("Não foi possível completar a operação", e);
        } finally {
            context.remove();
            entityManager.close();
        }
    }

    @Override
    public <T> T execute(Supplier<T> action) {
        EntityManager entityManager = HibernateUtil.openEntityManager();
        context.set(entityManager);
        try {
            logger.debug("Iniciando transação");
            entityManager.getTransaction().begin();
            T result = action.get();
            entityManager.getTransaction().commit();
            logger.debug("Transação confirmada");
            return result;
        } catch (LibraryException e) {
            entityManager.getTransaction().rollback();
            logger.warn("Transação revertida: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            logger.warn("Transação revertida: {}", e.getMessage());
            throw new LibraryException("Não foi possível completar a operação", e);
        } finally {
            context.remove();
            entityManager.close();
        }
    }

    public static Optional<EntityManager> getCurrentEntityManager() {
        return Optional.ofNullable(context.get());
    }
}
