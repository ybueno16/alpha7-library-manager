package br.com.yuri.alpha7.infra.persistence.autor;

import br.com.yuri.alpha7.domain.autor.model.Autor;
import br.com.yuri.alpha7.domain.autor.repository.AutorRepository;
import br.com.yuri.alpha7.infra.persistence.BaseRepository;

import java.util.List;
import java.util.Optional;

public class AutorRepositoryImpl extends BaseRepository implements AutorRepository {
    @Override
    public Autor save(Autor autor) {
       return executeInTransaction(entityManager -> entityManager.merge(autor));
    }

    @Override
    public Optional<Autor> findById(Long id) {
        return executeQuery(entityManager -> Optional.ofNullable(entityManager.find(Autor.class, id)));
    }

    @Override
    public Optional<Autor> findByNome(String nome) {
        return executeQuery(entityManager -> {
            List<Autor> results = entityManager.createQuery(
                    "SELECT a FROM Autor a WHERE a.nome = :nome", Autor.class)
                    .setParameter("nome", nome)
                    .getResultList();
            return Optional.ofNullable(
                    results.isEmpty() ? null : results.get(0)
            );
        });
    }

    @Override
    public List<Autor> findAll() {
        return executeQuery(
                entityManager -> entityManager.createQuery
                        ("SELECT a FROM Autor a", Autor.class).getResultList()
        );
    }

    @Override
    public void delete(Long id) {
        executeInTransaction(entityManager -> {
            Autor autor = entityManager.find(Autor.class, id);
            if (autor != null) {
                entityManager.remove(autor);
            }
            return null;
        });
    }
}
