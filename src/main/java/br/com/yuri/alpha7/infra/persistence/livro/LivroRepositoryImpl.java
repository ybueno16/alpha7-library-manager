package br.com.yuri.alpha7.infra.persistence.livro;

import br.com.yuri.alpha7.domain.exception.BookNotFoundException;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.repository.LivroRepository;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import br.com.yuri.alpha7.infra.persistence.BaseRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Implementação JPA do {@link LivroRepository} usando Hibernate/JPA via {@link br.com.yuri.alpha7.infra.persistence.BaseRepository}.
 *
 * <p>Pontos de atenção na implementação:
 * <ul>
 *   <li>{@link #save(br.com.yuri.alpha7.domain.livro.model.Livro)} resolve os livros semelhantes
 *       via {@code em.getReference} para evitar carregar as entidades completas, já que só a
 *       referência de chave estrangeira é necessária para persistir o relacionamento.</li>
 *   <li>{@link #findAll()} e {@link #findByFiltro(String)} usam {@code LEFT JOIN FETCH} para
 *       carregar editora e autores em uma única query e evitar o problema N+1.</li>
 *   <li>{@link #findByIsbn(ISBN)} exclui registros com soft delete; {@link #findByIsbnIncludingDeleted(ISBN)}
 *       ignora o filtro, usado durante a importação para detectar e restaurar livros deletados.</li>
 *   <li>A exclusão é soft delete via JPQL {@code SET l.deletedAt = CURRENT_TIMESTAMP}.</li>
 * </ul>
 */
public class LivroRepositoryImpl extends BaseRepository implements LivroRepository {

    @Override
    public Livro save(Livro livro) {
        LivroEntity entity = LivroMapper.toEntity(livro);
        return executeInTransaction(em -> {
            List<LivroEntity> semelhantes = livro.getLivrosSemelhantes().stream()
                    .filter(s -> s.getId() != null)
                    .map(s -> em.getReference(LivroEntity.class, s.getId()))
                    .collect(Collectors.toList());
            entity.setLivrosSemelhantes(semelhantes);
            LivroEntity saved = em.merge(entity);
            return LivroMapper.toDomain(saved);
        });
    }

    @Override
    public Optional<Livro> findById(Long id) {
        return executeQuery(em -> {
            LivroEntity entity = em.find(LivroEntity.class, id);
            if (entity == null || entity.isDeleted()) {
                return Optional.empty();
            }
            return Optional.of(LivroMapper.toDomainWithSemelhantes(entity));
        });
    }

    @Override
    public Optional<Livro> findByIsbn(ISBN isbn) {
        return executeQuery(em ->
                em.createQuery(
                                "SELECT l FROM Livro l " +
                                "LEFT JOIN FETCH l.editora " +
                                "LEFT JOIN FETCH l.autores " +
                                "WHERE l.isbn = :isbn " +
                                "AND l.deletedAt IS NULL", LivroEntity.class)
                        .setParameter("isbn", isbn)
                        .getResultList()
                        .stream()
                        .findFirst()
                        .map(LivroMapper::toDomain));
    }

    @Override
    public Optional<Livro> findByIsbnIncludingDeleted(ISBN isbn) {
        return executeQuery(em ->
                em.createQuery(
                                "SELECT l FROM Livro l " +
                                "LEFT JOIN FETCH l.editora " +
                                "LEFT JOIN FETCH l.autores " +
                                "WHERE l.isbn = :isbn", LivroEntity.class)
                        .setParameter("isbn", isbn)
                        .getResultList()
                        .stream()
                        .findFirst()
                        .map(LivroMapper::toDomain));
    }

    @Override
    public List<Livro> findAll() {
        return executeQuery(em ->
                em.createQuery(
                                "SELECT DISTINCT l FROM Livro l " +
                                "LEFT JOIN FETCH l.editora " +
                                "LEFT JOIN FETCH l.autores " +
                                "WHERE l.deletedAt IS NULL " +
                                "ORDER BY l.titulo", LivroEntity.class)
                        .getResultList()
                        .stream()
                        .map(LivroMapper::toDomain)
                        .collect(Collectors.toList()));
    }

    @Override
    public List<Livro> findByFiltro(String termo) {
        String safe = termo.replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
        return executeQuery(em ->
                em.createQuery(
                                "SELECT DISTINCT l FROM Livro l " +
                                "LEFT JOIN FETCH l.editora e " +
                                "LEFT JOIN FETCH l.autores a " +
                                "WHERE l.deletedAt IS NULL " +
                                "AND (LOWER(l.titulo)         LIKE LOWER(:termo) ESCAPE '!' " +
                                "OR   LOWER(l.idioma)         LIKE LOWER(:termo) ESCAPE '!' " +
                                "OR   LOWER(e.nome)           LIKE LOWER(:termo) ESCAPE '!' " +
                                "OR   LOWER(a.nome)           LIKE LOWER(:termo) ESCAPE '!' " +
                                "OR   CAST(l.isbn AS string)  LIKE LOWER(:termo) ESCAPE '!') " +
                                "ORDER BY l.titulo", LivroEntity.class)
                        .setParameter("termo", "%" + safe + "%")
                        .getResultList()
                        .stream()
                        .map(LivroMapper::toDomain)
                        .collect(Collectors.toList()));
    }

    @Override
    public long countAll() {
        return executeQuery(em ->
                (Long) em.createQuery(
                        "SELECT COUNT(l) FROM Livro l WHERE l.deletedAt IS NULL")
                        .getSingleResult());
    }

    @Override
    public Map<String, Long> countByIdioma() {
        return executeQuery(em -> {
            List<Object[]> rows = em.createQuery(
                    "SELECT l.idioma, COUNT(l) FROM Livro l " +
                    "WHERE l.deletedAt IS NULL " +
                    "GROUP BY l.idioma", Object[].class)
                    .getResultList();
            Map<String, Long> result = new HashMap<>();
            for (Object[] row : rows) {
                result.put((String) row[0], (Long) row[1]);
            }
            return result;
        });
    }

    @Override
    public Map<String, Long> countByAutor() {
        return executeQuery(em -> {
            List<Object[]> rows = em.createQuery(
                    "SELECT a.nome, COUNT(l) FROM Livro l " +
                    "JOIN l.autores a " +
                    "WHERE l.deletedAt IS NULL " +
                    "GROUP BY a.nome", Object[].class)
                    .getResultList();
            Map<String, Long> result = new HashMap<>();
            for (Object[] row : rows) {
                result.put((String) row[0], (Long) row[1]);
            }
            return result;
        });
    }

    @Override
    public Map<String, Long> countByEditora() {
        return executeQuery(em -> {
            List<Object[]> rows = em.createQuery(
                    "SELECT e.nome, COUNT(l) FROM Livro l " +
                    "LEFT JOIN l.editora e " +
                    "WHERE l.deletedAt IS NULL " +
                    "GROUP BY e.nome", Object[].class)
                    .getResultList();
            Map<String, Long> result = new HashMap<>();
            for (Object[] row : rows) {
                result.put((String) row[0], (Long) row[1]);
            }
            return result;
        });
    }

    @Override
    public Map<Integer, Long> countByAno() {
        return executeQuery(em -> {
            List<Object[]> rows = em.createQuery(
                    "SELECT YEAR(l.dataPublicacao), COUNT(l) FROM Livro l " +
                    "WHERE l.deletedAt IS NULL AND l.dataPublicacao IS NOT NULL " +
                    "GROUP BY YEAR(l.dataPublicacao) " +
                    "ORDER BY YEAR(l.dataPublicacao)", Object[].class)
                    .getResultList();
            Map<Integer, Long> result = new TreeMap<>();
            for (Object[] row : rows) {
                result.put(((Number) row[0]).intValue(), (Long) row[1]);
            }
            return result;
        });
    }

    @Override
    public void delete(Long id) {
        executeInTransaction(em -> {
            int updated = em.createQuery(
                            "UPDATE Livro l " +
                            "SET l.deletedAt = CURRENT_TIMESTAMP " +
                            "WHERE l.id = :id AND l.deletedAt IS NULL")
                    .setParameter("id", id)
                    .executeUpdate();
            if (updated == 0) {
                throw new BookNotFoundException("Livro não encontrado");
            }
            em.getEntityManagerFactory().getCache().evict(LivroEntity.class, id);
            return null;
        });
    }
}
