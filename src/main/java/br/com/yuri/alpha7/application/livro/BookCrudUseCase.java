package br.com.yuri.alpha7.application.livro;

import br.com.yuri.alpha7.domain.exception.BookNotFoundException;
import br.com.yuri.alpha7.domain.exception.IsbnInvalidoException;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.repository.LivroRepository;

import java.util.Optional;

/**
 * Caso de uso responsável pelas operações de criação, leitura e exclusão de livros.
 */
public class BookCrudUseCase {

    private final LivroRepository livroRepository;

    public BookCrudUseCase(LivroRepository livroRepository) {
        this.livroRepository = livroRepository;
    }

    /**
     * Persiste um livro novo ou atualiza um existente.
     * Para criação, rejeita ISBNs já cadastrados por outro livro.
     *
     * @param livro livro a ser salvo
     * @return livro salvo com id preenchido
     * @throws IsbnInvalidoException se o ISBN já estiver cadastrado para outro livro
     */
    public Livro save(Livro livro) throws IsbnInvalidoException {
        Optional<Livro> existing = livroRepository.findByIsbn(livro.getIsbn());
        if (existing.isPresent() && !existing.get().getId().equals(livro.getId())) {
            throw new IsbnInvalidoException("Book with this ISBN is already registered: " + livro.getIsbn());
        }
        return livroRepository.save(livro);
    }

    /**
     * Busca um livro pelo seu identificador.
     *
     * @param id identificador do livro
     * @return {@link Optional} contendo o livro, nunca vazio
     * @throws BookNotFoundException se o livro não for encontrado
     */
    public Optional<Livro> findById(Long id) throws BookNotFoundException {
        return Optional.ofNullable(livroRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found")));
    }

    /**
     * Remove um livro do acervo.
     *
     * @param id identificador do livro
     * @throws BookNotFoundException se o livro não for encontrado
     */
    public void delete(Long id) throws BookNotFoundException {
        if (!livroRepository.findById(id).isPresent()) {
            throw new BookNotFoundException("Book not found");
        }
        livroRepository.delete(id);
    }
}
