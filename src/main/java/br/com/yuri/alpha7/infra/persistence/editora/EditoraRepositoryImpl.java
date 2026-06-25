package br.com.yuri.alpha7.infra.persistence.editora;

import br.com.yuri.alpha7.domain.editora.model.Editora;
import br.com.yuri.alpha7.domain.editora.repository.EditoraRepository;
import br.com.yuri.alpha7.infra.persistence.BaseRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class EditoraRepositoryImpl extends BaseRepository implements EditoraRepository {
    @Override
    public Editora save(Editora editora) {
        return executeInTransaction(entityManager -> entityManager.merge(editora));
    }

    @Override
    public Optional<Editora> findById(Long id) {
        return executeQuery(entityManager -> Optional.ofNullable(entityManager.find(Editora.class, id)));
    }

    @Override
    public Optional<Editora> findByNome(String nome) {
        return executeQuery(entityManager -> {
            List<Editora> results = entityManager.createQuery(
                    "SELECT e FROM Editora e WHERE e.nome = :nome", Editora.class)
                    .setParameter("nome", nome)
                    .getResultList();
            return Optional.ofNullable(results.isEmpty() ? null : results.get(0));
        });
    }

    @Override
    public List<Editora> findAll() {
        return executeQuery(
                entityManager -> entityManager.createQuery(
                        "SELECT e FROM Editora e", Editora.class)
                        .getResultList());
    }

    @Override
    public void delete(Long id) {
        executeInTransaction(entityManager -> {
            Editora editora = entityManager.find(Editora.class, id);
            if (editora != null) {
                entityManager.remove(editora);
            }
            return null;
        });
    }
}
