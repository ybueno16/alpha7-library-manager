package br.com.yuri.alpha7.application.importacao;

import br.com.yuri.alpha7.application.importacao.model.ImportRecord;
import br.com.yuri.alpha7.application.importacao.parser.XmlImportParser;
import br.com.yuri.alpha7.domain.exception.ImportException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class XmlImportParserTest {

    private final XmlImportParser parser = new XmlImportParser();

    @Test
    @DisplayName(
            "Given the parser instance," +
            " when supports is called," +
            " then 'xml' is returned"
    )
    void shouldSupportXmlExtension() {
        assertEquals("xml", parser.supports());
    }

    @Test
    @DisplayName(
            "Given a valid XML with one fully-filled book," +
            " when parse is called," +
            " then all fields are mapped to the ImportRecord"
    )
    void shouldParseAllFieldsFromValidXml() {
        String xml = "<livros><livro>" +
                "<titulo>Clean Code</titulo>" +
                "<isbn>9780132350884</isbn>" +
                "<autores>Robert C. Martin</autores>" +
                "<editora>Prentice Hall</editora>" +
                "<dataPublicacao>2008</dataPublicacao>" +
                "<idioma>eng</idioma>" +
                "<numeroPaginas>431</numeroPaginas>" +
                "</livro></livros>";

        List<ImportRecord> records = parser.parse(stream(xml));

        assertEquals(1, records.size());
        ImportRecord rec = records.get(0);
        assertEquals("Clean Code", rec.getTitulo());
        assertEquals("9780132350884", rec.getIsbn());
        assertEquals("Robert C. Martin", rec.getAutores());
        assertEquals("Prentice Hall", rec.getEditora());
        assertEquals("2008", rec.getDataPublicacao());
        assertEquals("eng", rec.getIdioma());
        assertEquals("431", rec.getNumeroPaginas());
    }

    @Test
    @DisplayName(
            "Given an XML where optional tags are absent," +
            " when parse is called," +
            " then the missing fields default to empty string"
    )
    void shouldDefaultMissingTagsToEmptyString() {
        String xml = "<livros><livro>" +
                "<titulo>Minimal</titulo>" +
                "<isbn>9780132350884</isbn>" +
                "<autores>Author</autores>" +
                "</livro></livros>";

        List<ImportRecord> records = parser.parse(stream(xml));

        assertEquals(1, records.size());
        ImportRecord rec = records.get(0);
        assertEquals("", rec.getEditora());
        assertEquals("", rec.getDataPublicacao());
        assertEquals("", rec.getIdioma());
        assertEquals("", rec.getNumeroPaginas());
    }

    @Test
    @DisplayName(
            "Given an XML with multiple books," +
            " when parse is called," +
            " then all books are returned in order"
    )
    void shouldParseMultipleBooks() {
        String xml = "<livros>" +
                "<livro><titulo>Book 1</titulo><isbn>9780132350884</isbn><autores>A</autores></livro>" +
                "<livro><titulo>Book 2</titulo><isbn>9780134685991</isbn><autores>B</autores></livro>" +
                "</livros>";

        List<ImportRecord> records = parser.parse(stream(xml));

        assertEquals(2, records.size());
        assertEquals("Book 1", records.get(0).getTitulo());
        assertEquals("Book 2", records.get(1).getTitulo());
    }

    @Test
    @DisplayName(
            "Given a malformed XML stream," +
            " when parse is called," +
            " then ImportException is thrown"
    )
    void shouldThrowImportExceptionWhenXmlIsMalformed() {
        assertThrows(ImportException.class, () ->
                parser.parse(stream("not valid xml <<< broken"))
        );
    }

    @Test
    @DisplayName(
            "Given an XML with a nested livro element inside another livro," +
            " when parse is called," +
            " then the nested element is ignored"
    )
    void shouldIgnoreNestedLivroElements() {
        String xml = "<livros>" +
                "<livro>" +
                "  <titulo>Outer</titulo>" +
                "  <isbn>9780132350884</isbn>" +
                "  <autores>Author</autores>" +
                "  <livro><titulo>Nested</titulo><isbn>9780134685991</isbn></livro>" +
                "</livro>" +
                "</livros>";

        List<ImportRecord> records = parser.parse(stream(xml));

        assertEquals(1, records.size());
        assertEquals("Outer", records.get(0).getTitulo());
    }

    private InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes());
    }
}
