package br.com.yuri.alpha7.domain.exception;

/**
 * Lançada quando um ISBN é inválido ou já pertence a outro livro no acervo.
 *
 * <p>Pode ser produzida em dois contextos distintos:
 * <ul>
 *   <li>Pela classe {@code ISBN} ao validar formato ou dígito verificador durante a construção
 *       do Value Object.</li>
 *   <li>Por {@code BookCrudUseCase} ao detectar que o ISBN informado já está cadastrado para
 *       um livro diferente do que está sendo editado.</li>
 * </ul>
 */
public class IsbnInvalidoException extends LibraryException {

    public IsbnInvalidoException(String message) {
        super(message);
    }
}
