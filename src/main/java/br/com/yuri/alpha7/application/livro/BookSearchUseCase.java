package br.com.yuri.alpha7.application.livro;

import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.repository.LivroRepository;

import java.util.List;

/**
 * Caso de uso responsável pela pesquisa de livros no acervo.
 */
public class BookSearchUseCase {

    private final LivroRepository livroRepository;

    public BookSearchUseCase(LivroRepository livroRepository) {
        this.livroRepository = livroRepository;
    }

    /**
     * Retorna todos os livros cadastrados.
     *
     * @return lista de livros, nunca nula
     */
    public List<Livro> findAll() {
        return livroRepository.findAll();
    }

    /**
     * Pesquisa livros por qualquer campo textual (título, autor, ISBN, editora, idioma).
     *
     * @param filter termo de pesquisa
     * @return lista de livros que correspondem ao filtro
     */
    public List<Livro> findByFiltro(String filter) {
        return livroRepository.findByFiltro(filter);
    }
}
