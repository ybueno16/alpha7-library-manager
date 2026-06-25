package br.com.yuri.alpha7.domain.exception;

public class ImportException extends LibraryException {

    public ImportException(String message) {
        super(message);
    }

    public ImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
