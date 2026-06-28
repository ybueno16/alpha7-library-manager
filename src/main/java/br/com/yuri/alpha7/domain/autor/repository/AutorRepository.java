package br.com.yuri.alpha7.domain.autor.repository;

import br.com.yuri.alpha7.domain.autor.model.Autor;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de acesso a dados para a entidade {@link Autor}.
 *
 * <p>A busca por nome ({@link #findByNome}) é o ponto de entrada principal durante a importação
 * de CSV e ao salvar um livro via formulário: o sistema verifica se o autor já existe antes de
 * criar um novo, evitando duplicatas.
 */
public interface AutorRepository {

    /**
     * Persiste um novo autor ou atualiza os dados de um existente.
     *
     * @param autor autor a ser salvo
     * @return autor salvo com {@code id} preenchido pelo banco
     */
    Autor save(Autor autor);

    /**
     * Busca um autor pelo identificador gerado pelo banco.
     *
     * @param id identificador do autor
     * @return {@link Optional} com o autor, ou vazio se não encontrado
     */
    Optional<Autor> findById(Long id);

    /**
     * Busca um autor pelo nome exato (case-sensitive).
     *
     * @param nome nome completo do autor
     * @return {@link Optional} com o autor, ou vazio se não encontrado
     */
    Optional<Autor> findByNome(String nome);

    /**
     * Retorna todos os autores cadastrados.
     *
     * @return lista de autores, nunca nula
     */
    List<Autor> findAll();

    /**
     * Remove um autor pelo identificador.
     *
     * @param id identificador do autor a remover
     */
    void delete(Long id);
}
