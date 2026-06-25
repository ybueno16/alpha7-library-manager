package br.com.yuri.alpha7.domain.livro.model;

import br.com.yuri.alpha7.domain.autor.model.Autor;
import br.com.yuri.alpha7.domain.editora.model.Editora;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class LivroTest {

    private static final ISBN ISBN_A = new ISBN("9780132350884");
    private static final ISBN ISBN_B = new ISBN("9780134685991");

    @Test
    @DisplayName(
            "Given the same book instance," +
            " when equals is called with itself," +
            " then true is returned"
    )
    void shouldReturnTrueWhenComparedToItself() {
        Livro livro = livroWith(ISBN_A);
        assertEquals(livro, livro);
    }

    @Test
    @DisplayName(
            "Given a book," +
            " when equals is called with null," +
            " then false is returned"
    )
    void shouldReturnFalseWhenComparedToNull() {
        assertFalse(livroWith(ISBN_A).equals(null));
    }

    @Test
    @DisplayName(
            "Given a book," +
            " when equals is called with a different type," +
            " then false is returned"
    )
    void shouldReturnFalseWhenComparedToDifferentType() {
        assertFalse(livroWith(ISBN_A).equals("not a book"));
    }

    @Test
    @DisplayName(
            "Given two books with the same ISBN," +
            " when equals is called," +
            " then true is returned"
    )
    void shouldReturnTrueWhenBooksHaveSameIsbn() {
        assertEquals(livroWith(ISBN_A), livroWith(ISBN_A));
    }

    @Test
    @DisplayName(
            "Given two books with different ISBNs," +
            " when equals is called," +
            " then false is returned"
    )
    void shouldReturnFalseWhenBooksHaveDifferentIsbns() {
        assertNotEquals(livroWith(ISBN_A), livroWith(ISBN_B));
    }

    @Test
    @DisplayName(
            "Given two books with the same ISBN," +
            " when hashCode is called on both," +
            " then the same value is returned"
    )
    void shouldReturnSameHashCodeForBooksWithSameIsbn() {
        assertEquals(livroWith(ISBN_A).hashCode(), livroWith(ISBN_A).hashCode());
    }

    @Test
    @DisplayName(
            "Given a book with title and ISBN," +
            " when toString is called," +
            " then a non-null string is returned"
    )
    void shouldReturnNonNullStringOnToString() {
        assertNotNull(livroWith(ISBN_A).toString());
    }

    @Test
    @DisplayName(
            "Given a book," +
            " when all fields are set," +
            " then all getters return the correct values"
    )
    void shouldReturnCorrectValuesForAllFields() {
        Editora editora = new Editora("Prentice Hall");
        Autor autor = new Autor("Robert Martin");

        Livro livro = new Livro();
        livro.setId(1L);
        livro.setTitulo("Clean Code");
        livro.setIsbn(ISBN_A);
        livro.setDataPublicacao(LocalDate.of(2008, 8, 1));
        livro.setNumeroPaginas(431);
        livro.setIdioma("eng");
        livro.setEditora(editora);
        livro.setAutores(Collections.singletonList(autor));
        livro.setLivrosSemelhantes(Collections.emptyList());

        assertEquals(1L, livro.getId());
        assertEquals("Clean Code", livro.getTitulo());
        assertEquals(ISBN_A, livro.getIsbn());
        assertEquals(LocalDate.of(2008, 8, 1), livro.getDataPublicacao());
        assertEquals(431, livro.getNumeroPaginas());
        assertEquals("eng", livro.getIdioma());
        assertEquals(editora, livro.getEditora());
        assertEquals(1, livro.getAutores().size());
        assertTrue(livro.getLivrosSemelhantes().isEmpty());
    }

    private Livro livroWith(ISBN isbn) {
        Livro livro = new Livro();
        livro.setIsbn(isbn);
        return livro;
    }
}
