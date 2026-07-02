package br.com.yuri.alpha7.domain.livro.repository;

import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Contrato de acesso a dados para a entidade {@link Livro}.
 */
public interface LivroRepository {

    /**
     * Persiste ou atualiza um livro.
     *
     * @param livro livro a ser salvo
     * @return livro salvo com id gerado
     */
    Livro save(Livro livro);

    /**
     * Busca um livro pelo seu identificador.
     *
     * @param id identificador
     * @return {@link Optional} com o livro, ou vazio se não encontrado
     */
    Optional<Livro> findById(Long id);

    /**
     * Busca um livro pelo ISBN, ignorando registros com soft delete.
     *
     * @param isbn ISBN do livro
     * @return {@link Optional} com o livro ativo, ou vazio se não encontrado
     */
    Optional<Livro> findByIsbn(ISBN isbn);

    /**
     * Busca um livro pelo ISBN independente do status de exclusão lógica.
     * Usado pela importação para localizar livros soft-deleted e reativá-los.
     *
     * @param isbn ISBN do livro
     * @return {@link Optional} com o livro (ativo ou excluído), ou vazio se nunca cadastrado
     */
    Optional<Livro> findByIsbnIncludingDeleted(ISBN isbn);

    List<Livro> findAll();

    PagedResult<Livro> findAll(int page, int pageSize);

    PagedResult<Livro> findByFiltro(LivroFiltro filtro, int page, int pageSize);

    List<String> findAllEditorasAtivas();

    List<String> findAllIdiomasDistintos();

    /**
     * Remove um livro pelo identificador.
     *
     * @param id identificador do livro a remover
     */
    void delete(Long id);

    /** Retorna o total de livros ativos no acervo. */
    long countAll();

    /**
     * Retorna a contagem de livros agrupados por idioma.
     * Chaves podem ser {@code null} ou string em branco para livros sem idioma.
     */
    Map<String, Long> countByIdioma();

    /**
     * Retorna a contagem de livros por nome de autor.
     */
    Map<String, Long> countByAutor();

    /**
     * Retorna a contagem de livros por nome de editora.
     * Chave {@code null} representa livros sem editora.
     */
    Map<String, Long> countByEditora();

    /**
     * Retorna a contagem de livros por ano de publicação, ordenada por ano crescente.
     * Livros sem data de publicação são excluídos.
     */
    Map<Integer, Long> countByAno();
}
