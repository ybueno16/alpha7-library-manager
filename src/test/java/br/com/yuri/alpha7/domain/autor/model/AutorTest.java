package br.com.yuri.alpha7.domain.autor.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AutorTest {

    @Test
    @DisplayName(
            "Given the same author instance," +
            " when equals is called with itself," +
            " then true is returned"
    )
    void shouldReturnTrueWhenComparedToItself() {
        Autor autor = new Autor("Joshua Bloch");
        assertEquals(autor, autor);
    }

    @Test
    @DisplayName(
            "Given an author," +
            " when equals is called with null," +
            " then false is returned"
    )
    void shouldReturnFalseWhenComparedToNull() {
        assertFalse(new Autor("Joshua Bloch").equals(null));
    }

    @Test
    @DisplayName(
            "Given an author," +
            " when equals is called with a different type," +
            " then false is returned"
    )
    void shouldReturnFalseWhenComparedToDifferentType() {
        assertFalse(new Autor("Joshua Bloch").equals("Joshua Bloch"));
    }

    @Test
    @DisplayName(
            "Given two authors with the same name," +
            " when equals is called," +
            " then true is returned"
    )
    void shouldReturnTrueWhenAuthorsHaveSameName() {
        assertEquals(new Autor("Joshua Bloch"), new Autor("Joshua Bloch"));
    }

    @Test
    @DisplayName(
            "Given two authors with different names," +
            " when equals is called," +
            " then false is returned"
    )
    void shouldReturnFalseWhenAuthorsHaveDifferentNames() {
        assertNotEquals(new Autor("Joshua Bloch"), new Autor("Robert Martin"));
    }

    @Test
    @DisplayName(
            "Given two authors with the same name," +
            " when hashCode is called on both," +
            " then the same value is returned"
    )
    void shouldReturnSameHashCodeForAuthorsWithSameName() {
        assertEquals(new Autor("Joshua Bloch").hashCode(), new Autor("Joshua Bloch").hashCode());
    }

    @Test
    @DisplayName(
            "Given an author with a name," +
            " when toString is called," +
            " then the name is returned"
    )
    void shouldReturnNameOnToString() {
        assertEquals("Joshua Bloch", new Autor("Joshua Bloch").toString());
    }

    @Test
    @DisplayName(
            "Given an author," +
            " when all fields are set," +
            " then all getters return the correct values"
    )
    void shouldReturnCorrectValuesForAllFields() {
        Autor autor = new Autor();
        autor.setId(1L);
        autor.setNome("Edsger Dijkstra");
        autor.setDataNascimento(LocalDate.of(1930, 5, 11));
        autor.setDataFalecimento(LocalDate.of(2002, 8, 6));
        autor.setBio("Pioneer of computer science.");

        assertEquals(1L, autor.getId());
        assertEquals("Edsger Dijkstra", autor.getNome());
        assertEquals(LocalDate.of(1930, 5, 11), autor.getDataNascimento());
        assertEquals(LocalDate.of(2002, 8, 6), autor.getDataFalecimento());
        assertEquals("Pioneer of computer science.", autor.getBio());
    }
}
