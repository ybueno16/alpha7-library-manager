package br.com.yuri.alpha7.infra.client.openlibrary;

import br.com.yuri.alpha7.domain.exception.OpenLibraryUnavailableException;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import br.com.yuri.alpha7.infra.client.openlibrary.mapper.OpenLibraryBookMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class OpenLibraryClientImplTest {

    private MockWebServer mockWebServer;
    private OpenLibraryClientImpl client;

    private static final ISBN VALID_ISBN = new ISBN("9780132350884");

    private static final String BOOK_JSON = "{"
            + "\"title\": \"Clean Code\","
            + "\"number_of_pages\": 431,"
            + "\"publish_date\": \"2008-08-01\","
            + "\"languages\": [{\"key\": \"/languages/eng\"}],"
            + "\"publishers\": [\"Prentice Hall\"],"
            + "\"authors\": [{\"key\": \"/authors/OL25678A\"}]"
            + "}";

    private static final String AUTHOR_JSON = "{"
            + "\"name\": \"Robert C. Martin\","
            + "\"birth_date\": \"5 December 1952\","
            + "\"bio\": {\"type\": \"/type/text\", \"value\": \"Software engineer and author.\"}"
            + "}";

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .build();

        String baseUrl = "http://localhost:" + mockWebServer.getPort();
        client = new OpenLibraryClientImpl(httpClient, new ObjectMapper(), new OpenLibraryBookMapper(), baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName(
            "Given the OpenLibrary API returns 404 for an ISBN," +
            " when findByIsbn is called," +
            " then empty optional is returned"
    )
    void shouldReturnEmptyWhenApiReturns404() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        Optional<Livro> result = client.findByIsbn(VALID_ISBN);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName(
            "Given the OpenLibrary API returns a server error," +
            " when findByIsbn is called," +
            " then OpenLibraryUnavailableException is thrown"
    )
    void shouldThrowExceptionWhenApiReturnsServerError() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        assertThrows(OpenLibraryUnavailableException.class, () -> client.findByIsbn(VALID_ISBN));
    }

    @Test
    @DisplayName(
            "Given a valid book response with a resolvable author," +
            " when findByIsbn is called," +
            " then a mapped Livro with author data is returned"
    )
    void shouldReturnMappedLivroWithAuthorWhenApiReturnsValidResponse() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(BOOK_JSON)
                .addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(AUTHOR_JSON)
                .addHeader("Content-Type", "application/json"));

        Optional<Livro> result = client.findByIsbn(VALID_ISBN);

        assertTrue(result.isPresent());
        Livro livro = result.get();
        assertEquals("Clean Code", livro.getTitulo());
        assertEquals(431, livro.getNumeroPaginas());
        assertEquals("eng", livro.getIdioma());
        assertEquals(1, livro.getAutores().size());
        assertEquals("Robert C. Martin", livro.getAutores().get(0).getNome());
    }

    @Test
    @DisplayName(
            "Given a valid book response but the author endpoint fails," +
            " when findByIsbn is called," +
            " then the book is returned without that author"
    )
    void shouldReturnLivroWithoutAuthorWhenAuthorEndpointFails() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(BOOK_JSON)
                .addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        Optional<Livro> result = client.findByIsbn(VALID_ISBN);

        assertTrue(result.isPresent());
        assertTrue(result.get().getAutores().isEmpty());
    }

    @Test
    @DisplayName(
            "Given a network failure when calling OpenLibrary," +
            " when findByIsbn is called," +
            " then OpenLibraryUnavailableException is thrown"
    )
    void shouldThrowExceptionOnNetworkFailure() {
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));

        assertThrows(OpenLibraryUnavailableException.class, () -> client.findByIsbn(VALID_ISBN));
    }
}
