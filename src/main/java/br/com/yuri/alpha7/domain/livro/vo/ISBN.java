package br.com.yuri.alpha7.domain.livro.vo;

import br.com.yuri.alpha7.domain.exception.IsbnInvalidoException;
import java.io.Serializable;
import java.util.Objects;

/**
 * Value Object que representa um ISBN (International Standard Book Number).
 * Suporta ISBN-10 e ISBN-13, com validação do dígito verificador.
 */
public final class ISBN implements Serializable {

    private final String value;

    /**
     * @param value ISBN em formato ISBN-10 ou ISBN-13, com ou sem hífens
     * @throws IsbnInvalidoException se o formato ou dígito verificador for inválido
     */
    public ISBN(String value) {
        if (value == null) {
            throw new IsbnInvalidoException("ISBN não pode ser nulo");
        }
        String normalized = normalize(value);
        validate(normalized);
        this.value = normalized.length() == 10 ? toIsbn13(normalized) : normalized;
    }

    private static String toIsbn13(String isbn10) {
        String base = "978" + isbn10.substring(0, 9);
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += (base.charAt(i) - '0') * (i % 2 == 0 ? 1 : 3);
        }
        return base + (10 - (sum % 10)) % 10;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ISBN)) return false;
        return Objects.equals(value, ((ISBN) o).value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }

    private static String normalize(String value) {
        return value.replaceAll("[-\\s]", "").toUpperCase();
    }

    private static void validate(String isbn) {
        if (isbn.length() == 10) {
            validateIsbn10(isbn);
            return;
        }
        if (isbn.length() == 13) {
            validateIsbn13(isbn);
            return;
        }
        throw new IsbnInvalidoException("ISBN deve ter 10 ou 13 caracteres: " + isbn);
    }

    private static void validateIsbn10(String isbn) {
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            if (!Character.isDigit(isbn.charAt(i))) {
                throw new IsbnInvalidoException("ISBN-10 contém caractere inválido: " + isbn);
            }
            sum += (isbn.charAt(i) - '0') * (10 - i);
        }
        char last = isbn.charAt(9);
        if (last != 'X' && !Character.isDigit(last)) {
            throw new IsbnInvalidoException("ISBN-10 contém caractere inválido: " + isbn);
        }
        sum += (last == 'X') ? 10 : (last - '0');

        if (sum % 11 != 0) {
            throw new IsbnInvalidoException("Dígito verificador ISBN-10 inválido: " + isbn);
        }
    }

    private static void validateIsbn13(String isbn) {
        int sum = 0;
        for (int i = 0; i < 13; i++) {
            if (!Character.isDigit(isbn.charAt(i))) {
                throw new IsbnInvalidoException("ISBN-13 contém caractere inválido: " + isbn);
            }
            int weight = (i % 2 == 0) ? 1 : 3;
            sum += (isbn.charAt(i) - '0') * weight;
        }

        if (sum % 10 != 0) {
            throw new IsbnInvalidoException("Dígito verificador ISBN-13 inválido: " + isbn);
        }
    }
}
