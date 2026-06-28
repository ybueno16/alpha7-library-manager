package br.com.yuri.alpha7.domain.exception;

/**
 * Lançada quando um livro pesquisado por ID não é encontrado no acervo.
 *
 * <p>É uma {@link RuntimeException} (via {@link LibraryException}), portanto não precisa ser
 * declarada na assinatura dos métodos, mas deve ser documentada via {@code @throws} nos casos
 * de uso que a produzem.
 */
public class BookNotFoundException extends LibraryException {

    public BookNotFoundException(String message) {
        super(message);
    }
}
