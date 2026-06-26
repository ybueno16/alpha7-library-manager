package br.com.yuri.alpha7.infra.persistence.editora;

import br.com.yuri.alpha7.domain.editora.model.Editora;
import br.com.yuri.alpha7.domain.editora.repository.EditoraRepository;
import br.com.yuri.alpha7.infra.persistence.BaseRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
            return Optional.ofNullable(EditoraMapper.toDomain(entity));
        });
    }

    @Override
    public Optional<Editora> findByNome(String nome) {
        return executeQuery(em -> {
            List<EditoraEntity> results = em.createQuery(
                            "SELECT e FROM Editora e WHERE e.nome = :nome", EditoraEntity.class)
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
                em.createQuery("SELECT e FROM Editora e", EditoraEntity.class)
                        .getResultList()
                        .stream()
                        .map(EditoraMapper::toDomain)
                        .collect(Collectors.toList()));
    }

    @Override
    public void delete(Long id) {
        executeInTransaction(em -> {
            EditoraEntity entity = em.find(EditoraEntity.class, id);
            if (entity != null) {
                em.remove(entity);
            }
            return null;
        });
    }
}
