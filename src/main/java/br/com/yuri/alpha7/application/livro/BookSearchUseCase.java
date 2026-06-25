package br.com.yuri.alpha7.application.livro;

import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.repository.LivroRepository;

import java.util.List;

public class BookSearchUseCase {

    private final LivroRepository livroRepository;

    public BookSearchUseCase(LivroRepository livroRepository) {
        this.livroRepository = livroRepository;
    }

    public List<Livro> findAll() {
        return livroRepository.findAll();
    }

    public List<Livro> findByFiltro(String filter) {
        return livroRepository.findByFiltro(filter);
    }
}
