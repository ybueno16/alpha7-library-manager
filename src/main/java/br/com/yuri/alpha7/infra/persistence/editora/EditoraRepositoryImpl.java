package br.com.yuri.alpha7.infra.persistence.editora;

import br.com.yuri.alpha7.domain.editora.model.Editora;
import br.com.yuri.alpha7.domain.editora.repository.EditoraRepository;
import br.com.yuri.alpha7.domain.exception.LibraryException;
import br.com.yuri.alpha7.infra.persistence.BaseRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementação JPA do {@link EditoraRepository} usando Hibernate/JPA via {@link br.com.yuri.alpha7.infra.persistence.BaseRepository}.
 *
 * <p>A operação mais utilizada em produção é {@link #findByNome(String)}, chamada pelo
 * {@link br.com.yuri.alpha7.application.editora.EditoraUseCase} antes de cada salvamento de livro
 * para evitar duplicatas de editora com o mesmo nome. A exclusão segue o padrão de soft delete
 * idêntico ao {@link br.com.yuri.alpha7.infra.persistence.autor.AutorRepositoryImpl}.
 */
public class EditoraRepositoryImpl extends BaseRepository implements EditoraRepository {

    @Override
    public Editora save(Editora editora) {
        EditoraEntity entity = EditoraMapper.toEntity(editora);
        EditoraEntity saved = executeInTransaction(em -> em.merge(entity));
        return EditoraMapper.toDomain(saved);
    }

    @Override
    public Optional<Editora> findById(Long id) {
        return executeQuery(em -> {
            EditoraEntity entity = em.find(EditoraEntity.class, id);
            if (entity == null || entity.isDeleted()) {
                return Optional.empty();
            }
            return Optional.of(EditoraMapper.toDomain(entity));
        });
    }

    @Override
    public Optional<Editora> findByNome(String nome) {
        return executeQuery(em -> {
            List<EditoraEntity> results = em.createQuery(
                            "SELECT e FROM Editora e " +
                            "WHERE LOWER(TRIM(e.nome)) = LOWER(TRIM(:nome)) " +
                            "AND e.deletedAt IS NULL", EditoraEntity.class)
                    .setParameter("nome", nome)
                    .getResultList();
            if (results.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(EditoraMapper.toDomain(results.get(0)));
        });
    }

    @Override
    public List<Editora> findAll() {
        return executeQuery(em ->
                em.createQuery(
                                "SELECT e FROM Editora e " +
                                "WHERE e.deletedAt IS NULL", EditoraEntity.class)
                        .getResultList()
                        .stream()
                        .map(EditoraMapper::toDomain)
                        .collect(Collectors.toList()));
    }

    @Override
    public void delete(Long id) {
        executeInTransaction(em -> {
            int updated = em.createQuery(
                            "UPDATE Editora e " +
                            "SET e.deletedAt = CURRENT_TIMESTAMP " +
                            "WHERE e.id = :id AND e.deletedAt IS NULL")
                    .setParameter("id", id)
                    .executeUpdate();
            if (updated == 0) {
                throw new LibraryException("Editora não encontrada: id=" + id);
            }
            return null;
        });
    }
}
