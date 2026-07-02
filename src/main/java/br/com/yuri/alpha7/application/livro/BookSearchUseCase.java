package br.com.yuri.alpha7.application.livro;

import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.repository.LivroFiltro;
import br.com.yuri.alpha7.domain.livro.repository.LivroRepository;
import br.com.yuri.alpha7.domain.livro.repository.PagedResult;

import java.util.List;

/**
 * Caso de uso responsável por consultar livros do acervo.
 *
 * <p>Oferece listagem completa, listagem paginada, busca por filtros combinados
 * (termo geral, autor, editora, idioma e intervalo de ano) e consulta dos valores
 * distintos de editora e idioma usados para popular os filtros da tela de listagem.
 */
public class BookSearchUseCase {

    private final LivroRepository livroRepository;

    public BookSearchUseCase(LivroRepository livroRepository) {
        this.livroRepository = livroRepository;
    }

    /**
     * Retorna todos os livros ativos do acervo, sem paginação.
     *
     * @return lista completa de livros
     */
    public List<Livro> findAll() {
        return livroRepository.findAll();
    }

    /**
     * Retorna uma página de livros ativos do acervo.
     *
     * @param page     número da página, começando em zero
     * @param pageSize quantidade de itens por página
     * @return página de livros correspondente
     */
    public PagedResult<Livro> findAll(int page, int pageSize) {
        return livroRepository.findAll(page, pageSize);
    }

    /**
     * Retorna uma página de livros que atendem aos filtros informados.
     *
     * @param filtro   critérios de busca combinados
     * @param page     número da página, começando em zero
     * @param pageSize quantidade de itens por página
     * @return página de livros que atendem ao filtro
     */
    public PagedResult<Livro> findByFiltro(LivroFiltro filtro, int page, int pageSize) {
        return livroRepository.findByFiltro(filtro, page, pageSize);
    }

    /**
     * Retorna os nomes distintos de editoras com ao menos um livro ativo no acervo.
     *
     * @return lista de nomes de editoras, usada para popular o filtro da tela
     */
    public List<String> findAllEditorasAtivas() {
        return livroRepository.findAllEditorasAtivas();
    }

    /**
     * Retorna os idiomas distintos usados por livros ativos no acervo.
     *
     * @return lista de idiomas, usada para popular o filtro da tela
     */
    public List<String> findAllIdiomasDistintos() {
        return livroRepository.findAllIdiomasDistintos();
    }
}
