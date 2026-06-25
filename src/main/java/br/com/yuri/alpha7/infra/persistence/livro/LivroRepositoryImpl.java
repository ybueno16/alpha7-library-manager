package br.com.yuri.alpha7.infra.persistence.livro;

import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.repository.LivroRepository;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import br.com.yuri.alpha7.infra.persistence.BaseRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class LivroRepositoryImpl extends BaseRepository implements LivroRepository {
    @Override
    public Livro save(Livro livro) {
        return executeInTransaction(entityManager -> entityManager.merge(livro));
    }

    @Override
    public Optional<Livro> findById(Long id) {
        return executeQuery(entityManager -> Optional.ofNullable(entityManager.find(Livro.class, id)));
    }

    @Override
    public Optional<Livro> findByIsbn(ISBN isbn) {
        return executeQuery(entityManager -> {
            List<Livro> result = entityManager.createQuery(
                            "SELECT l FROM Livro l WHERE l.isbn = :isbn", Livro.class)
                    .setParameter("isbn", isbn)
                    .getResultList();
            return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
        });
    }

    @Override
    public List<Livro> findAll() {
        return executeQuery(em ->
                em.createQuery(
                                "SELECT DISTINCT l FROM Livro l " +
                                        "LEFT JOIN FETCH l.editora " +
                                        "LEFT JOIN FETCH l.autores " +
                                        "ORDER BY l.titulo", Livro.class)
                        .getResultList()
        );
    }

    @Override
    public List<Livro> findByFiltro(String termo) {
        return executeQuery(em ->
                em.createQuery(
                                "SELECT DISTINCT l FROM Livro l " +
                                        "LEFT JOIN FETCH l.editora e " +
                                        "LEFT JOIN FETCH l.autores a " +
                                        "WHERE LOWER(l.titulo)    LIKE LOWER(:termo) " +
                                        "OR    LOWER(l.idioma)    LIKE LOWER(:termo) " +
                                        "OR    LOWER(e.nome)      LIKE LOWER(:termo) " +
                                        "OR    LOWER(a.nome)      LIKE LOWER(:termo) " +
                                        "OR    CAST(l.isbn AS string) LIKE LOWER(:termo) " +
                                        "ORDER BY l.titulo", Livro.class)
                        .setParameter("termo", "%" + termo + "%")
                        .getResultList()
        );
    }

    @Override
    public void delete(Long id) {
        executeInTransaction(entityManager -> {
            Livro livro = entityManager.find(Livro.class, id);
            if (livro != null) {
                entityManager.remove(livro);
            }
            return null;
        });
    }
}
