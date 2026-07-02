package br.com.yuri.alpha7.application.importacao;

import br.com.yuri.alpha7.application.importacao.model.ImportRecord;
import br.com.yuri.alpha7.application.importacao.parser.CsvImportParser;
import br.com.yuri.alpha7.domain.exception.ImportException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvImportParserTest {

    private final CsvImportParser parser = new CsvImportParser();

    @Test
    @DisplayName(
            "Given a valid CSV stream without BOM," +
            " when parse is called," +
            " then all records are returned"
    )
    void shouldParseValidCsvWithoutBom() {
        String csv = "titulo,isbn,autores,editora,dataPublicacao,idioma,numeroPaginas\n" +
                "Clean Code,9780132350884,Robert Martin,Prentice Hall,2008,en,431";

        List<ImportRecord> records = parser.parse(stream(csv));

        assertEquals(1, records.size());
        assertEquals("Clean Code",    records.get(0).getTitulo());
        assertEquals("9780132350884", records.get(0).getIsbn());
        assertEquals("Robert Martin", records.get(0).getAutores());
    }

    @Test
    @DisplayName(
            "Given a CSV stream starting with a UTF-8 BOM," +
            " when parse is called," +
            " then the BOM is stripped and records are returned"
    )
    void shouldStripBomAndParseRecords() {
        String bom = "﻿";
        String csv = bom + "titulo,isbn,autores,editora,dataPublicacao,idioma,numeroPaginas\n" +
                "Effective Java,9780134685991,Joshua Bloch,,,, ";

        List<ImportRecord> records = parser.parse(stream(csv));

        assertEquals(1, records.size());
        assertEquals("Effective Java", records.get(0).getTitulo());
    }

    @Test
    @DisplayName(
            "Given a completely empty CSV stream," +
            " when parse is called," +
            " then an empty list is returned"
    )
    void shouldReturnEmptyListForEmptyStream() {
        List<ImportRecord> records = parser.parse(stream(""));
        assertTrue(records.isEmpty());
    }

    @Test
    @DisplayName(
            "Given an InputStream that throws an IOException on the first read," +
            " when parse is called," +
            " then ImportException is thrown"
    )
    void shouldThrowImportExceptionWhenStreamFails() {
        InputStream broken = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("simulated disk error");
            }
        };

        assertThrows(ImportException.class, () -> parser.parse(broken));
    }

    private InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }
}
