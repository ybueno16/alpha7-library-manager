package br.com.yuri.alpha7.domain.editora.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EditoraTest {

    @Test
    @DisplayName(
            "Given the same publisher instance," +
            " when equals is called with itself," +
            " then true is returned"
    )
    void shouldReturnTrueWhenComparedToItself() {
        Editora editora = new Editora("O'Reilly");
        assertEquals(editora, editora);
    }

    @Test
    @DisplayName(
            "Given a publisher," +
            " when equals is called with null," +
            " then false is returned"
    )
    void shouldReturnFalseWhenComparedToNull() {
        assertFalse(new Editora("O'Reilly").equals(null));
    }

    @Test
    @DisplayName(
            "Given a publisher," +
            " when equals is called with a different type," +
            " then false is returned"
    )
    void shouldReturnFalseWhenComparedToDifferentType() {
        assertFalse(new Editora("O'Reilly").equals("O'Reilly"));
    }

    @Test
    @DisplayName(
            "Given two publishers with the same name," +
            " when equals is called," +
            " then true is returned"
    )
    void shouldReturnTrueWhenPublishersHaveSameName() {
        assertEquals(new Editora("Manning"), new Editora("Manning"));
    }

    @Test
    @DisplayName(
            "Given two publishers with different names," +
            " when equals is called," +
            " then false is returned"
    )
    void shouldReturnFalseWhenPublishersHaveDifferentNames() {
        assertNotEquals(new Editora("Manning"), new Editora("Packt"));
    }

    @Test
    @DisplayName(
            "Given two publishers with the same name," +
            " when hashCode is called on both," +
            " then the same value is returned"
    )
    void shouldReturnSameHashCodeForPublishersWithSameName() {
        assertEquals(new Editora("Manning").hashCode(), new Editora("Manning").hashCode());
    }

    @Test
    @DisplayName(
            "Given a publisher with a name," +
            " when toString is called," +
            " then the name is returned"
    )
    void shouldReturnNameOnToString() {
        assertEquals("Manning", new Editora("Manning").toString());
    }

    @Test
    @DisplayName(
            "Given a publisher," +
            " when id and name are set," +
            " then getters return correct values"
    )
    void shouldReturnCorrectValuesForAllFields() {
        Editora editora = new Editora();
        editora.setId(1L);
        editora.setNome("Addison-Wesley");

        assertEquals(1L, editora.getId());
        assertEquals("Addison-Wesley", editora.getNome());
    }
}
