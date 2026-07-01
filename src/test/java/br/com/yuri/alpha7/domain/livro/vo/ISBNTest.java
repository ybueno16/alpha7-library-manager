package br.com.yuri.alpha7.domain.livro.vo;

import br.com.yuri.alpha7.domain.exception.IsbnInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ISBNTest {

    @Test
    @DisplayName(
            "Given a valid ISBN-13," +
            " when ISBN is created," +
            " then value is normalized and stored"
    )
    void shouldCreateValidIsbn13() {
        ISBN isbn = new ISBN("978-0-13-235088-4");
        assertEquals("9780132350884", isbn.getValue());
    }

    @Test
    @DisplayName(
            "Given a valid ISBN-10," +
            " when ISBN is created," +
            " then value is stored correctly"
    )
    void shouldCreateValidIsbn10() {
        ISBN isbn = new ISBN("0306406152");
        assertEquals("0306406152", isbn.getValue());
    }

    @Test
    @DisplayName(
            "Given a valid ISBN-10 with X as check digit," +
            " when ISBN is created," +
            " then it is accepted"
    )
    void shouldAcceptIsbn10WithXCheckDigit() {
        assertDoesNotThrow(() -> new ISBN("080442957X"));
    }

    @Test
    @DisplayName(
            "Given a null value," +
            " when ISBN is created," +
            " then IsbnInvalidoException is thrown"
    )
    void shouldThrowWhenValueIsNull() {
        assertThrows(IsbnInvalidoException.class, () -> new ISBN(null));
    }

    @Test
    @DisplayName(
            "Given a string with wrong length," +
            " when ISBN is created," +
            " then IsbnInvalidoException is thrown"
    )
    void shouldThrowWhenLengthIsInvalid() {
        assertThrows(IsbnInvalidoException.class, () -> new ISBN("12345"));
    }

    @Test
    @DisplayName(
            "Given an ISBN-13 with invalid check digit," +
            " when ISBN is created," +
            " then IsbnInvalidoException is thrown"
    )
    void shouldThrowWhenIsbn13CheckDigitIsInvalid() {
        assertThrows(IsbnInvalidoException.class, () -> new ISBN("9780132350885"));
    }

    @Test
    @DisplayName(
            "Given an ISBN-13 with a non-digit character," +
            " when ISBN is created," +
            " then IsbnInvalidoException is thrown"
    )
    void shouldThrowWhenIsbn13ContainsNonDigit() {
        assertThrows(IsbnInvalidoException.class, () -> new ISBN("97801323508!4"));
    }

    @Test
    @DisplayName(
            "Given an ISBN-10 with invalid check digit," +
            " when ISBN is created," +
            " then IsbnInvalidoException is thrown"
    )
    void shouldThrowWhenIsbn10CheckDigitIsInvalid() {
        assertThrows(IsbnInvalidoException.class, () -> new ISBN("0306406151"));
    }

    @Test
    @DisplayName(
            "Given an ISBN-10 with a non-digit character in the last position," +
            " when ISBN is created," +
            " then IsbnInvalidoException is thrown"
    )
    void shouldThrowWhenIsbn10ContainsNonDigitAtLastPosition() {
        assertThrows(IsbnInvalidoException.class, () -> new ISBN("030640615!"));
    }

    @Test
    @DisplayName(
            "Given an ISBN-10 with a non-digit character in an interior position," +
            " when ISBN is created," +
            " then IsbnInvalidoException is thrown"
    )
    void shouldThrowWhenIsbn10ContainsNonDigitInInteriorPosition() {
        assertThrows(IsbnInvalidoException.class, () -> new ISBN("0A0640615X"));
    }

    @Test
    @DisplayName(
            "Given two ISBNs with the same value," +
            " when equals is called," +
            " then true is returned"
    )
    void shouldReturnTrueWhenIsbnsAreEqual() {
        assertEquals(new ISBN("9780132350884"), new ISBN("9780132350884"));
    }

    @Test
    @DisplayName(
            "Given an ISBN," +
            " when equals is called with itself," +
            " then true is returned"
    )
    void shouldReturnTrueWhenComparedToItself() {
        ISBN isbn = new ISBN("9780132350884");
        assertEquals(isbn, isbn);
    }

    @Test
    @DisplayName(
            "Given an ISBN," +
            " when equals is called with null," +
            " then false is returned"
    )
    void shouldReturnFalseWhenComparedToNull() {
        assertFalse(new ISBN("9780132350884").equals(null));
    }

    @Test
    @DisplayName(
            "Given two ISBNs with the same value," +
            " when hashCode is called on both," +
            " then the same value is returned"
    )
    void shouldReturnSameHashCodeForEqualIsbns() {
        assertEquals(
                new ISBN("9780132350884").hashCode(),
                new ISBN("9780132350884").hashCode()
        );
    }

    @Test
    @DisplayName(
            "Given an ISBN," +
            " when toString is called," +
            " then the normalized value is returned"
    )
    void shouldReturnNormalizedValueOnToString() {
        assertEquals("9780132350884", new ISBN("9780132350884").toString());
    }
}
