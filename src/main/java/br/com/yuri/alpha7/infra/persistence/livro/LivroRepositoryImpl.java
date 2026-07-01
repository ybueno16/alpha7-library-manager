package br.com.yuri.alpha7.infra.persistence.livro;

import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.repository.LivroRepository;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import br.com.yuri.alpha7.infra.persistence.BaseRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
                    .filter(Objects::nonNull)
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
        return executeQuery(em -> {
            List<LivroEntity> result = em.createQuery(
                            "SELECT l FROM Livro l " +
                            "LEFT JOIN FETCH l.editora " +
                            "LEFT JOIN FETCH l.autores " +
                            "WHERE l.isbn = :isbn " +
                            "AND l.deletedAt IS NULL", LivroEntity.class)
                    .setParameter("isbn", isbn)
                    .getResultList();
            if (result.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(LivroMapper.toDomain(result.get(0)));
        });
    }

    @Override
    public Optional<Livro> findByIsbnIncludingDeleted(ISBN isbn) {
        return executeQuery(em -> {
            List<LivroEntity> result = em.createQuery(
                            "SELECT l FROM Livro l " +
                            "LEFT JOIN FETCH l.editora " +
                            "LEFT JOIN FETCH l.autores " +
                            "WHERE l.isbn = :isbn", LivroEntity.class)
                    .setParameter("isbn", isbn)
                    .getResultList();
            if (result.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(LivroMapper.toDomain(result.get(0)));
        });
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
    public void delete(Long id) {
        executeInTransaction(em -> {
            LivroEntity entity = em.find(LivroEntity.class, id);
            if (entity != null) {
                em.createQuery(
                                "UPDATE Livro l " +
                                "SET l.deletedAt = CURRENT_TIMESTAMP " +
                                "WHERE l.id = :id")
                        .setParameter("id", id)
                        .executeUpdate();
            }
            return null;
        });
    }
}
