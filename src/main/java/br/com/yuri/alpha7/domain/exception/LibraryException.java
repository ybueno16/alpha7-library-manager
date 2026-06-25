package br.com.yuri.alpha7.domain.exception;

/**
 * Exceção base para erros de domínio da biblioteca.
 */
public class LibraryException extends RuntimeException {

    public LibraryException(String message) {
        super(message);
    }

    public LibraryException(String message, Throwable cause) {
        super(message, cause);
    }
}
