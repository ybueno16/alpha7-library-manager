package br.com.yuri.alpha7.application.isbn;

import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.repository.LivroRepository;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;

import java.util.Optional;

public class IsbnLookupUseCase {

    private final LivroRepository livroRepository;
    private final OpenLibraryClient openLibraryClient;

    public IsbnLookupUseCase(LivroRepository livroRepository, OpenLibraryClient openLibraryClient) {
        this.livroRepository = livroRepository;
        this.openLibraryClient = openLibraryClient;
    }

    public Optional<Livro> findByIsbn(ISBN isbn) {
        Optional<Livro> local = livroRepository.findByIsbn(isbn);
        if (local.isPresent()) {
            return local;
        }
        return openLibraryClient.findByIsbn(isbn);
    }
}
