package br.com.yuri.alpha7.application.isbn;

import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;

import java.util.Optional;

public interface OpenLibraryClient {
    Optional<Livro> findByIsbn(ISBN isbn);
}
