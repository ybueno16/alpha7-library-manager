package br.com.yuri.alpha7.application.livro;

import br.com.yuri.alpha7.domain.exception.BookNotFoundException;
import br.com.yuri.alpha7.domain.exception.IsbnInvalidoException;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.repository.LivroRepository;

import java.util.Optional;

public class BookCrudUseCase {

    private final LivroRepository livroRepository;

    public BookCrudUseCase(LivroRepository livroRepository) {
        this.livroRepository = livroRepository;
    }

    public Livro save(Livro livro) throws IsbnInvalidoException {
        if (livroRepository.findByIsbn(livro.getIsbn()).isPresent()) {
            throw new IsbnInvalidoException("Book with this ISBN is already registered: " + livro.getIsbn());
        }
        return livroRepository.save(livro);
    }

    public Optional<Livro> findById(Long id) throws BookNotFoundException {
        return Optional.ofNullable(livroRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found")));
    }

    public void delete(Long id) throws BookNotFoundException {
        if (!livroRepository.findById(id).isPresent()) {
            throw new BookNotFoundException("Book not found");
        }
        livroRepository.delete(id);
    }
}
