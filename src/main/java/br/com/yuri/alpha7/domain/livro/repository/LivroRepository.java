package br.com.yuri.alpha7.domain.livro.repository;

import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;

import java.util.List;
import java.util.Optional;

public interface LivroRepository {

    Livro save(Livro livro);

    Optional<Livro> findById(Long id);

    Optional<Livro> findByIsbn(ISBN isbn);

    List<Livro> findAll();

    List<Livro> findByFiltro(String termo);

    void delete(Long id);
}
