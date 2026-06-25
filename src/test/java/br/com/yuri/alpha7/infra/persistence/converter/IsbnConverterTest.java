package br.com.yuri.alpha7.infra.persistence.converter;

import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IsbnConverterTest {

    private final IsbnConverter converter = new IsbnConverter();

    @Test
    @DisplayName(
            "Given a valid ISBN," +
            " when convertToDatabaseColumn is called," +
            " then the normalized string value is returned"
    )
    void shouldReturnStringValueWhenIsbnIsValid() {
        assertEquals("9780132350884", converter.convertToDatabaseColumn(new ISBN("9780132350884")));
    }

    @Test
    @DisplayName(
            "Given a null ISBN," +
            " when convertToDatabaseColumn is called," +
            " then null is returned"
    )
    void shouldReturnNullWhenIsbnIsNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    @DisplayName(
            "Given a valid ISBN string," +
            " when convertToEntityAttribute is called," +
            " then an ISBN value object is returned"
    )
    void shouldReturnIsbnWhenStringIsValid() {
        ISBN isbn = converter.convertToEntityAttribute("9780132350884");
        assertNotNull(isbn);
        assertEquals("9780132350884", isbn.getValue());
    }

    @Test
    @DisplayName(
            "Given a null string," +
            " when convertToEntityAttribute is called," +
            " then null is returned"
    )
    void shouldReturnNullWhenStringIsNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }
}
