package br.com.yuri.alpha7.infra.persistence.autor;

import br.com.yuri.alpha7.domain.autor.model.Autor;
import br.com.yuri.alpha7.domain.autor.repository.AutorRepository;
import br.com.yuri.alpha7.infra.persistence.BaseRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementação JPA do {@link AutorRepository} usando Hibernate/JPA via {@link br.com.yuri.alpha7.infra.persistence.BaseRepository}.
 *
 * <p>Todas as operações de escrita participam de uma transação gerenciada por
 * {@code executeInTransaction}. As operações de leitura usam {@code executeQuery},
 * que abre um {@code EntityManager} sem transação explícita.
 *
 * <p>A exclusão é implementada como soft delete: em vez de remover o registro, o campo
 * {@code deleted_at} é atualizado via JPQL para o timestamp atual. Consultas de leitura
 * filtram automaticamente registros com {@code deleted_at IS NOT NULL}.
 */
public class AutorRepositoryImpl extends BaseRepository implements AutorRepository {

    @Override
    public Autor save(Autor autor) {
        AutorEntity entity = AutorMapper.toEntity(autor);
        AutorEntity saved = executeInTransaction(em -> em.merge(entity));
        return AutorMapper.toDomain(saved);
    }

    @Override
    public Optional<Autor> findById(Long id) {
        return executeQuery(em -> {
            AutorEntity entity = em.find(AutorEntity.class, id);
            if (entity == null || entity.isDeleted()) {
                return Optional.empty();
            }
            return Optional.of(AutorMapper.toDomain(entity));
        });
    }

    @Override
    public Optional<Autor> findByNome(String nome) {
        return executeQuery(em -> {
            List<AutorEntity> results = em.createQuery(
                            "SELECT a FROM Autor a " +
                            "WHERE LOWER(a.nome) = LOWER(:nome) " +
                            "AND a.deletedAt IS NULL", AutorEntity.class)
                    .setParameter("nome", nome)
                    .getResultList();
            if (results.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(AutorMapper.toDomain(results.get(0)));
        });
    }

    @Override
    public List<Autor> findAll() {
        return executeQuery(em ->
                em.createQuery(
                                "SELECT a FROM Autor a " +
                                "WHERE a.deletedAt IS NULL", AutorEntity.class)
                        .getResultList()
                        .stream()
                        .map(AutorMapper::toDomain)
                        .collect(Collectors.toList()));
    }

    @Override
    public void delete(Long id) {
        executeInTransaction(em -> {
            AutorEntity entity = em.find(AutorEntity.class, id);
            if (entity != null) {
                em.createQuery(
                                "UPDATE Autor a " +
                                "SET a.deletedAt = CURRENT_TIMESTAMP " +
                                "WHERE a.id = :id")
                        .setParameter("id", id)
                        .executeUpdate();
            }
            return null;
        });
    }
}
