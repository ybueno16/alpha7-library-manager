package br.com.yuri.alpha7.application.livro;

import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.repository.LivroFiltro;
import br.com.yuri.alpha7.domain.livro.repository.LivroRepository;
import br.com.yuri.alpha7.domain.livro.repository.PagedResult;

import java.util.List;

public class BookSearchUseCase {

    private final LivroRepository livroRepository;

    public BookSearchUseCase(LivroRepository livroRepository) {
        this.livroRepository = livroRepository;
    }

    public List<Livro> findAll() {
        return livroRepository.findAll();
    }

    public PagedResult<Livro> findAll(int page, int pageSize) {
        return livroRepository.findAll(page, pageSize);
    }

    public PagedResult<Livro> findByFiltro(LivroFiltro filtro, int page, int pageSize) {
        return livroRepository.findByFiltro(filtro, page, pageSize);
    }

    public List<String> findAllEditorasAtivas() {
        return livroRepository.findAllEditorasAtivas();
    }

    public List<String> findAllIdiomasDistintos() {
        return livroRepository.findAllIdiomasDistintos();
    }
}
