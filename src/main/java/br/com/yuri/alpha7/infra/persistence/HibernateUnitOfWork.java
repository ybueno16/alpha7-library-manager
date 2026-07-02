package br.com.yuri.alpha7.infra.persistence;

import br.com.yuri.alpha7.application.UnitOfWork;
import br.com.yuri.alpha7.domain.exception.IsbnInvalidoException;
import br.com.yuri.alpha7.domain.exception.LibraryException;
import org.hibernate.exception.ConstraintViolationException;
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
        inTransaction(() -> {
            action.run();
            return null;
        });
    }

    @Override
    public <T> T execute(Supplier<T> action) {
        return inTransaction(action);
    }

    static ConstraintViolationException unwrapConstraintViolation(Throwable t) {
        Throwable cause = t;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException) {
                return (ConstraintViolationException) cause;
            }
            cause = cause.getCause();
        }
        return null;
    }

    public static Optional<EntityManager> getCurrentEntityManager() {
        return Optional.ofNullable(context.get());
    }

    private <T> T inTransaction(Supplier<T> action) {
        EntityManager em = HibernateUtil.openEntityManager();
        context.set(em);
        try {
            logger.debug("Iniciando transação");
            em.getTransaction().begin();
            T result = action.get();
            em.getTransaction().commit();
            logger.debug("Transação confirmada");
            return result;
        } catch (LibraryException e) {
            em.getTransaction().rollback();
            logger.warn("Transação revertida: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            em.getTransaction().rollback();
            ConstraintViolationException cve = unwrapConstraintViolation(e);
            if (cve != null) {
                logger.warn("Transação revertida por violação de constraint: {}", cve.getMessage());
                if (cve.getConstraintName() != null && cve.getConstraintName().toLowerCase().contains("isbn")) {
                    throw new IsbnInvalidoException("ISBN já cadastrado no acervo");
                }
                throw new LibraryException("Violação de regra de integridade dos dados", cve);
            }
            logger.warn("Transação revertida: {}", e.getMessage());
            throw new LibraryException("Não foi possível completar a operação", e);
        } finally {
            context.remove();
            em.close();
        }
    }
}
