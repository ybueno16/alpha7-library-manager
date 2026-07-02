package br.com.yuri.alpha7.application.livro;

import br.com.yuri.alpha7.application.UnitOfWork;
import br.com.yuri.alpha7.domain.autor.model.Autor;
import br.com.yuri.alpha7.domain.autor.repository.AutorRepository;
import br.com.yuri.alpha7.domain.editora.model.Editora;
import br.com.yuri.alpha7.domain.editora.repository.EditoraRepository;
import br.com.yuri.alpha7.domain.exception.BookNotFoundException;
import br.com.yuri.alpha7.domain.exception.IsbnInvalidoException;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.repository.LivroRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Caso de uso responsável por persistir, buscar e remover livros do acervo.
 *
 * <p>A principal regra de negócio encapsulada aqui é a unicidade do ISBN: ao salvar um livro,
 * o caso de uso verifica se já existe outro livro com o mesmo ISBN e rejeita a operação se
 * for uma criação (id nulo) ou se o ISBN pertencer a um livro diferente do que está sendo editado.
 * Esse controle previne ISBNs duplicados no acervo mesmo que a constraint do banco de dados
 * ainda não tenha sido avaliada.
 */
public class BookCrudUseCase {

    private final LivroRepository   livroRepository;
    private final AutorRepository   autorRepository;
    private final EditoraRepository editoraRepository;
    private final UnitOfWork        unitOfWork;

    public BookCrudUseCase(LivroRepository livroRepository,
                           AutorRepository autorRepository,
                           EditoraRepository editoraRepository,
                           UnitOfWork unitOfWork) {
        this.livroRepository   = livroRepository;
        this.autorRepository   = autorRepository;
        this.editoraRepository = editoraRepository;
        this.unitOfWork        = unitOfWork;
    }

    /**
     * Persiste um livro novo ou atualiza um existente.
     * Para criação, rejeita ISBNs já cadastrados por outro livro.
     *
     * @param livro livro a ser salvo
     * @return livro salvo com id preenchido
     * @throws IsbnInvalidoException se o ISBN já estiver cadastrado para outro livro
     */
    public Livro saveWithEditora(Livro livro, String editoraNome) {
        return unitOfWork.execute(() -> {
            if (editoraNome != null && !editoraNome.isEmpty()) {
                Editora editora = editoraRepository.findByNome(editoraNome)
                        .orElseGet(() -> editoraRepository.save(new Editora(editoraNome)));
                livro.setEditora(editora);
            }
            List<Autor> autoresResolvidos = livro.getAutores().stream()
                    .map(a -> autorRepository.findByNome(a.getNome())
                            .orElseGet(() -> autorRepository.save(a)))
                    .collect(Collectors.toList());
            livro.setAutores(autoresResolvidos);
            return save(livro);
        });
    }

    Livro save(Livro livro) throws IsbnInvalidoException {
        Optional<Livro> existing = livroRepository.findByIsbn(livro.getIsbn());
        if (existing.isPresent() && isIsbnFromDifferentBook(livro.getId(), existing.get().getId())) {
            throw new IsbnInvalidoException("ISBN já cadastrado no acervo: " + livro.getIsbn());
        }
        return livroRepository.save(livro);
    }

    /**
     * Busca um livro pelo seu identificador.
     *
     * @param id identificador do livro
     * @return livro encontrado
     * @throws BookNotFoundException se o livro não for encontrado
     */
    public Livro findById(Long id) throws BookNotFoundException {
        return livroRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Livro não encontrado: id=" + id));
    }

    /**
     * Remove um livro do acervo.
     *
     * @param id identificador do livro
     * @throws BookNotFoundException se o livro não for encontrado
     */
    public void delete(Long id) throws BookNotFoundException {
        livroRepository.delete(id);
    }

    /**
     * Verifica se o ISBN encontrado pertence a um livro diferente do que está sendo salvo —
     * um id nulo indica criação, então qualquer ISBN já existente é conflito.
     *
     * @param newId      id do livro sendo salvo, ou {@code null} se for uma criação
     * @param existingId id do livro dono do ISBN encontrado no acervo
     * @return {@code true} se o ISBN já pertence a outro livro
     */
    private boolean isIsbnFromDifferentBook(Long newId, Long existingId) {
        return newId == null || !newId.equals(existingId);
    }
}
