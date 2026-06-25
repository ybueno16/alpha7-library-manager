package br.com.yuri.alpha7.infra.client.openlibrary.mapper;

import br.com.yuri.alpha7.domain.autor.model.Autor;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import br.com.yuri.alpha7.infra.client.openlibrary.dto.OpenLibraryAuthorResponse;
import br.com.yuri.alpha7.infra.client.openlibrary.dto.OpenLibraryBookResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OpenLibraryBookMapperTest {

    private final OpenLibraryBookMapper mapper = new OpenLibraryBookMapper();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final ISBN VALID_ISBN = new ISBN("9780132350884");

    @Test
    @DisplayName(
            "Given a complete book response with author data," +
            " when toLivro is called," +
            " then all fields are mapped correctly"
    )
    void shouldMapAllFieldsWhenResponseIsComplete() throws Exception {
        OpenLibraryBookResponse response = new OpenLibraryBookResponse();
        response.setTitle("Clean Code");
        response.setNumberOfPages(431);
        response.setPublishDate("2008-08-01");

        OpenLibraryBookResponse.LanguageRef langRef = new OpenLibraryBookResponse.LanguageRef();
        langRef.setKey("/languages/eng");
        response.setLanguages(Collections.singletonList(langRef));

        OpenLibraryAuthorResponse authorResponse = new OpenLibraryAuthorResponse();
        authorResponse.setName("Robert C. Martin");
        authorResponse.setBirthDate("5 December 1952");
        authorResponse.setBio(objectMapper.readTree("{\"type\": \"/type/text\", \"value\": \"Software engineer.\"}"));

        Livro livro = mapper.toLivro(response, Collections.singletonList(authorResponse), VALID_ISBN);

        assertEquals("Clean Code", livro.getTitulo());
        assertEquals(431, livro.getNumeroPaginas());
        assertEquals(LocalDate.of(2008, 8, 1), livro.getDataPublicacao());
        assertEquals("eng", livro.getIdioma());
        assertEquals(1, livro.getAutores().size());

        Autor autor = livro.getAutores().get(0);
        assertEquals("Robert C. Martin", autor.getNome());
        assertEquals(LocalDate.of(1952, 12, 5), autor.getDataNascimento());
        assertEquals("Software engineer.", autor.getBio());
    }

    @Test
    @DisplayName(
            "Given a book response with an unparseable publish date," +
            " when toLivro is called," +
            " then dataPublicacao is null"
    )
    void shouldLeavePublishDateNullWhenDateIsUnparseable() {
        OpenLibraryBookResponse response = new OpenLibraryBookResponse();
        response.setTitle("Some Book");
        response.setPublishDate("not a date");
        response.setLanguages(Collections.emptyList());

        Livro livro = mapper.toLivro(response, Collections.emptyList(), VALID_ISBN);

        assertNull(livro.getDataPublicacao());
    }

    @Test
    @DisplayName(
            "Given an author response with bio as a JSON object," +
            " when toLivro is called," +
            " then the bio value is extracted correctly"
    )
    void shouldExtractBioFromObjectFormat() throws Exception {
        OpenLibraryBookResponse response = new OpenLibraryBookResponse();
        response.setTitle("Book");
        response.setLanguages(Collections.emptyList());

        OpenLibraryAuthorResponse authorResponse = new OpenLibraryAuthorResponse();
        authorResponse.setName("Author Name");
        authorResponse.setBio(objectMapper.readTree("{\"type\": \"/type/text\", \"value\": \"Bio from object.\"}"));

        Livro livro = mapper.toLivro(response, Collections.singletonList(authorResponse), VALID_ISBN);

        assertEquals("Bio from object.", livro.getAutores().get(0).getBio());
    }

    @Test
    @DisplayName(
            "Given an author response with bio as a plain string," +
            " when toLivro is called," +
            " then the bio string is used directly"
    )
    void shouldExtractBioFromStringFormat() throws Exception {
        OpenLibraryBookResponse response = new OpenLibraryBookResponse();
        response.setTitle("Book");
        response.setLanguages(Collections.emptyList());

        OpenLibraryAuthorResponse authorResponse = new OpenLibraryAuthorResponse();
        authorResponse.setName("Author Name");
        authorResponse.setBio(objectMapper.readTree("\"Bio as plain string.\""));

        Livro livro = mapper.toLivro(response, Collections.singletonList(authorResponse), VALID_ISBN);

        assertEquals("Bio as plain string.", livro.getAutores().get(0).getBio());
    }

    @Test
    @DisplayName(
            "Given an author response with a null name," +
            " when toLivro is called," +
            " then that author is excluded from the list"
    )
    void shouldSkipAuthorWithNullName() {
        OpenLibraryBookResponse response = new OpenLibraryBookResponse();
        response.setTitle("Book");
        response.setLanguages(Collections.emptyList());

        OpenLibraryAuthorResponse withName = new OpenLibraryAuthorResponse();
        withName.setName("Valid Author");

        OpenLibraryAuthorResponse withoutName = new OpenLibraryAuthorResponse();

        List<OpenLibraryAuthorResponse> authors = Arrays.asList(withName, withoutName);
        Livro livro = mapper.toLivro(response, authors, VALID_ISBN);

        assertEquals(1, livro.getAutores().size());
        assertEquals("Valid Author", livro.getAutores().get(0).getNome());
    }

    @Test
    @DisplayName(
            "Given an author response with bio as a JSON object without a 'value' key," +
            " when toLivro is called," +
            " then bio is left null"
    )
    void shouldLeaveAuthorBioNullWhenBioObjectHasNoValueKey() throws Exception {
        OpenLibraryBookResponse response = new OpenLibraryBookResponse();
        response.setTitle("Book");
        response.setLanguages(Collections.emptyList());

        OpenLibraryAuthorResponse authorResponse = new OpenLibraryAuthorResponse();
        authorResponse.setName("Author Name");
        authorResponse.setBio(objectMapper.readTree("{\"type\": \"/type/text\"}"));

        Livro livro = mapper.toLivro(response, Collections.singletonList(authorResponse), VALID_ISBN);

        assertNull(livro.getAutores().get(0).getBio());
    }

    @Test
    @DisplayName(
            "Given an author with birth date in 'MMMM d, yyyy' format," +
            " when toLivro is called," +
            " then the date is parsed correctly"
    )
    void shouldParseAuthorDateInMonthFullDayYearFormat() {
        OpenLibraryBookResponse response = new OpenLibraryBookResponse();
        response.setTitle("Book");
        response.setLanguages(Collections.emptyList());

        OpenLibraryAuthorResponse author = new OpenLibraryAuthorResponse();
        author.setName("Author");
        author.setBirthDate("December 5, 1952");

        Livro livro = mapper.toLivro(response, Collections.singletonList(author), VALID_ISBN);

        assertEquals(LocalDate.of(1952, 12, 5), livro.getAutores().get(0).getDataNascimento());
    }

    @Test
    @DisplayName(
            "Given an author with birth date in 'd MMM yyyy' format," +
            " when toLivro is called," +
            " then the date is parsed correctly"
    )
    void shouldParseAuthorDateInDayAbbrevMonthYearFormat() {
        OpenLibraryBookResponse response = new OpenLibraryBookResponse();
        response.setTitle("Book");
        response.setLanguages(Collections.emptyList());

        OpenLibraryAuthorResponse author = new OpenLibraryAuthorResponse();
        author.setName("Author");
        author.setBirthDate("5 Dec 1952");

        Livro livro = mapper.toLivro(response, Collections.singletonList(author), VALID_ISBN);

        assertEquals(LocalDate.of(1952, 12, 5), livro.getAutores().get(0).getDataNascimento());
    }

    @Test
    @DisplayName(
            "Given an author with birth date in 'MMM d, yyyy' format," +
            " when toLivro is called," +
            " then the date is parsed correctly"
    )
    void shouldParseAuthorDateInAbbrevMonthDayYearFormat() {
        OpenLibraryBookResponse response = new OpenLibraryBookResponse();
        response.setTitle("Book");
        response.setLanguages(Collections.emptyList());

        OpenLibraryAuthorResponse author = new OpenLibraryAuthorResponse();
        author.setName("Author");
        author.setBirthDate("Dec 5, 1952");

        Livro livro = mapper.toLivro(response, Collections.singletonList(author), VALID_ISBN);

        assertEquals(LocalDate.of(1952, 12, 5), livro.getAutores().get(0).getDataNascimento());
    }

    @Test
    @DisplayName(
            "Given an author with birth date in ISO format," +
            " when toLivro is called," +
            " then the date is parsed correctly"
    )
    void shouldParseAuthorDateInIsoFormat() {
        OpenLibraryBookResponse response = new OpenLibraryBookResponse();
        response.setTitle("Book");
        response.setLanguages(Collections.emptyList());

        OpenLibraryAuthorResponse author = new OpenLibraryAuthorResponse();
        author.setName("Author");
        author.setBirthDate("1952-12-05");

        Livro livro = mapper.toLivro(response, Collections.singletonList(author), VALID_ISBN);

        assertEquals(LocalDate.of(1952, 12, 5), livro.getAutores().get(0).getDataNascimento());
    }

    @Test
    @DisplayName(
            "Given a book response with publish date in 'MMM dd, yyyy' format," +
            " when toLivro is called," +
            " then the date is parsed correctly"
    )
    void shouldParsePublishDateInMmmDdYyyyFormat() {
        OpenLibraryBookResponse response = new OpenLibraryBookResponse();
        response.setTitle("Book");
        response.setPublishDate("Jan 01, 2008");
        response.setLanguages(Collections.emptyList());

        Livro livro = mapper.toLivro(response, Collections.emptyList(), VALID_ISBN);

        assertEquals(LocalDate.of(2008, 1, 1), livro.getDataPublicacao());
    }

    @Test
    @DisplayName(
            "Given a response with a null language list," +
            " when toLivro is called," +
            " then idioma is left null"
    )
    void shouldLeaveIdiomaNullWhenLanguagesIsNull() {
        OpenLibraryBookResponse response = new OpenLibraryBookResponse();
        response.setTitle("Book");
        response.setLanguages(null);

        Livro livro = mapper.toLivro(response, Collections.emptyList(), VALID_ISBN);

        assertNull(livro.getIdioma());
    }

    @Test
    @DisplayName(
            "Given a response with a language entry that has a null key," +
            " when toLivro is called," +
            " then idioma is left null"
    )
    void shouldLeaveIdiomaEmptyWhenLanguageKeyIsNull() {
        OpenLibraryBookResponse response = new OpenLibraryBookResponse();
        response.setTitle("Book");

        OpenLibraryBookResponse.LanguageRef langRef = new OpenLibraryBookResponse.LanguageRef();
        response.setLanguages(Collections.singletonList(langRef));

        Livro livro = mapper.toLivro(response, Collections.emptyList(), VALID_ISBN);

        assertNull(livro.getIdioma());
    }
}
