package br.com.yuri.alpha7.application.isbn;

import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.repository.LivroRepository;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;

import java.util.Optional;

/**
 * Caso de uso para busca de livros por ISBN.
 * Consulta primeiro a base local; caso não encontrado, recorre à API da OpenLibrary.
 */
public class IsbnLookupUseCase {

    private final LivroRepository livroRepository;
    private final OpenLibraryClient openLibraryClient;

    public IsbnLookupUseCase(LivroRepository livroRepository, OpenLibraryClient openLibraryClient) {
        this.livroRepository = livroRepository;
        this.openLibraryClient = openLibraryClient;
    }

    /**
     * Busca um livro pelo ISBN, priorizando o acervo local.
     * Se não encontrado localmente, consulta a API da OpenLibrary.
     *
     * @param isbn ISBN do livro
     * @return {@link Optional} com o livro encontrado, ou vazio se não existir
     */
    public Optional<Livro> findByIsbn(ISBN isbn) {
        Optional<Livro> local = livroRepository.findByIsbn(isbn);
        if (local.isPresent()) {
            return local;
        }
        return openLibraryClient.findByIsbn(isbn);
    }
}
