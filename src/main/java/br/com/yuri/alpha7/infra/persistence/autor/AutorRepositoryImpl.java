package br.com.yuri.alpha7.infra.persistence.autor;

import br.com.yuri.alpha7.domain.autor.model.Autor;
import br.com.yuri.alpha7.domain.autor.repository.AutorRepository;
import br.com.yuri.alpha7.infra.persistence.BaseRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
            return Optional.ofNullable(AutorMapper.toDomain(entity));
        });
    }

    @Override
    public Optional<Autor> findByNome(String nome) {
        return executeQuery(em -> {
            List<AutorEntity> results = em.createQuery(
                            "SELECT a FROM Autor a WHERE a.nome = :nome", AutorEntity.class)
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
                em.createQuery("SELECT a FROM Autor a", AutorEntity.class)
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
                em.remove(entity);
            }
            return null;
        });
    }
}
