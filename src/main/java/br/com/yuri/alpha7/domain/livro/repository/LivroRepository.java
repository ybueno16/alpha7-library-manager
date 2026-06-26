package br.com.yuri.alpha7.domain.livro.repository;

import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;

import java.util.List;
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
     * Busca um livro pelo ISBN.
     *
     * @param isbn ISBN do livro
     * @return {@link Optional} com o livro, ou vazio se não encontrado
     */
    Optional<Livro> findByIsbn(ISBN isbn);

    /**
     * Retorna todos os livros cadastrados.
     *
     * @return lista de livros
     */
    List<Livro> findAll();

    /**
     * Pesquisa livros por qualquer campo textual.
     *
     * @param termo termo de pesquisa
     * @return lista de livros que correspondem ao termo
     */
    List<Livro> findByFiltro(String termo);

    /**
     * Remove um livro pelo identificador.
     *
     * @param id identificador do livro a remover
     */
    void delete(Long id);
}
