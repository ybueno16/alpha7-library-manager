package br.com.yuri.alpha7.domain.editora.repository;

import br.com.yuri.alpha7.domain.editora.model.Editora;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de acesso a dados para a entidade {@link Editora}.
 *
 * <p>A busca por nome ({@link #findByNome}) é o ponto de entrada principal ao cadastrar ou
 * importar livros: o sistema verifica se a editora já existe antes de criar uma nova,
 * garantindo que livros com a mesma editora compartilhem o mesmo registro no banco.
 */
public interface EditoraRepository {

    /**
     * Persiste uma nova editora ou atualiza os dados de uma existente.
     *
     * @param editora editora a ser salva
     * @return editora salva com {@code id} preenchido pelo banco
     */
    Editora save(Editora editora);

    /**
     * Busca uma editora pelo identificador gerado pelo banco.
     *
     * @param id identificador da editora
     * @return {@link Optional} com a editora, ou vazio se não encontrada
     */
    Optional<Editora> findById(Long id);

    /**
     * Busca uma editora pelo nome exato (case-sensitive).
     *
     * @param nome nome da editora
     * @return {@link Optional} com a editora, ou vazio se não encontrada
     */
    Optional<Editora> findByNome(String nome);

    /**
     * Retorna todas as editoras cadastradas.
     *
     * @return lista de editoras, nunca nula
     */
    List<Editora> findAll();

    /**
     * Remove uma editora pelo identificador.
     *
     * @param id identificador da editora a remover
     */
    void delete(Long id);
}
