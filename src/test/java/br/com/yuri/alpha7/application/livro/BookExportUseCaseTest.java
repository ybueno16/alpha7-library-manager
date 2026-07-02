package br.com.yuri.alpha7.application.livro;

import br.com.yuri.alpha7.domain.autor.model.Autor;
import br.com.yuri.alpha7.domain.editora.model.Editora;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.repository.LivroRepository;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookExportUseCaseTest {

    @Mock LivroRepository livroRepository;
    @InjectMocks BookExportUseCase useCase;

    @Test
    @DisplayName(
            "Given an empty collection," +
            " when exportToCsv is called," +
            " then only the header row is written and zero is returned"
    )
    void shouldWriteOnlyHeaderWhenCollectionIsEmpty() throws IOException {
        when(livroRepository.findAll()).thenReturn(Collections.emptyList());
        StringWriter writer = new StringWriter();

        int count = useCase.exportToCsv(writer);

        assertEquals(0, count);
        String csv = writer.toString();
        assertTrue(csv.contains("titulo"));
        assertTrue(csv.contains("isbn"));
    }

    @Test
    @DisplayName(
            "Given a book with all fields filled," +
            " when exportToCsv is called," +
            " then all fields appear in the CSV output"
    )
    void shouldExportAllFieldsWhenBookIsComplete() throws IOException {
        Livro livro = new Livro();
        livro.setTitulo("Clean Code");
        livro.setIsbn(new ISBN("9780132350884"));
        livro.setDataPublicacao(LocalDate.of(2008, 8, 1));
        livro.setIdioma("English");
        livro.setNumeroPaginas(431);
        livro.setEditora(new Editora("Prentice Hall"));
        livro.getAutores().add(new Autor("Robert Martin"));

        when(livroRepository.findAll()).thenReturn(Collections.singletonList(livro));
        StringWriter writer = new StringWriter();

        int count = useCase.exportToCsv(writer);

        assertEquals(1, count);
        String csv = writer.toString();
        assertTrue(csv.contains("Clean Code"));
        assertTrue(csv.contains("9780132350884"));
        assertTrue(csv.contains("Robert Martin"));
        assertTrue(csv.contains("Prentice Hall"));
        assertTrue(csv.contains("2008-08-01"));
        assertTrue(csv.contains("English"));
        assertTrue(csv.contains("431"));
    }

    @Test
    @DisplayName(
            "Given a book with null optional fields," +
            " when exportToCsv is called," +
            " then null values are replaced with empty strings"
    )
    void shouldUseEmptyStringsForNullOptionalFields() throws IOException {
        Livro livro = new Livro();
        livro.setTitulo("Minimal Book");
        livro.setIsbn(new ISBN("9780132350884"));

        when(livroRepository.findAll()).thenReturn(Collections.singletonList(livro));
        StringWriter writer = new StringWriter();

        int count = useCase.exportToCsv(writer);

        assertEquals(1, count);
        assertFalse(writer.toString().contains("null"));
    }

    @Test
    @DisplayName(
            "Given a book with multiple authors," +
            " when exportToCsv is called," +
            " then author names are joined with a semicolon and space"
    )
    void shouldJoinMultipleAuthorNamesWithSemicolon() throws IOException {
        Livro livro = new Livro();
        livro.setTitulo("Co-authored Book");
        livro.setIsbn(new ISBN("9780132350884"));
        livro.getAutores().add(new Autor("Author A"));
        livro.getAutores().add(new Autor("Author B"));

        when(livroRepository.findAll()).thenReturn(Collections.singletonList(livro));
        StringWriter writer = new StringWriter();

        useCase.exportToCsv(writer);

        assertTrue(writer.toString().contains("Author A; Author B"));
    }

    @Test
    @DisplayName(
            "Given multiple books in the collection," +
            " when exportToCsv is called," +
            " then all books are exported and the total count is returned"
    )
    void shouldExportAllBooksAndReturnTotalCount() throws IOException {
        Livro livro1 = minimalBook("Book One", "9780132350884");
        Livro livro2 = minimalBook("Book Two", "9780134685991");

        when(livroRepository.findAll()).thenReturn(Arrays.asList(livro1, livro2));
        StringWriter writer = new StringWriter();

        int count = useCase.exportToCsv(writer);

        assertEquals(2, count);
        String csv = writer.toString();
        assertTrue(csv.contains("Book One"));
        assertTrue(csv.contains("Book Two"));
    }

    @Test
    @DisplayName(
            "Given multiple books in the collection," +
            " when exportToCsv is called with a progress callback," +
            " then callback receives current and total for each book exported"
    )
    void shouldInvokeProgressCallbackForEachBookExported() throws IOException {
        Livro livro1 = minimalBook("Book One", "9780132350884");
        Livro livro2 = minimalBook("Book Two", "9780134685991");
        when(livroRepository.findAll()).thenReturn(Arrays.asList(livro1, livro2));

        List<int[]> progress = new ArrayList<>();
        useCase.exportToCsv(new StringWriter(), (current, total) -> progress.add(new int[]{current, total}));

        assertEquals(2, progress.size());
        assertArrayEquals(new int[]{1, 2}, progress.get(0));
        assertArrayEquals(new int[]{2, 2}, progress.get(1));
    }

    private Livro minimalBook(String titulo, String isbn) {
        Livro livro = new Livro();
        livro.setTitulo(titulo);
        livro.setIsbn(new ISBN(isbn));
        return livro;
    }
}
