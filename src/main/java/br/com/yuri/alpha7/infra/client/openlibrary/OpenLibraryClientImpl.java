package br.com.yuri.alpha7.infra.client.openlibrary;

import br.com.yuri.alpha7.application.isbn.OpenLibraryClient;
import br.com.yuri.alpha7.domain.exception.OpenLibraryUnavailableException;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import br.com.yuri.alpha7.infra.client.openlibrary.dto.OpenLibraryAuthorResponse;
import br.com.yuri.alpha7.infra.client.openlibrary.dto.OpenLibraryBookResponse;
import br.com.yuri.alpha7.infra.client.openlibrary.mapper.OpenLibraryBookMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementação de {@link OpenLibraryClient} que consulta a API REST pública da OpenLibrary.
 *
 * <p>A busca de um livro envolve duas fases de requisição HTTP:
 * <ol>
 *   <li><strong>Busca do livro</strong> — {@code GET /isbn/{isbn}.json}: retorna título, editora,
 *       data de publicação, idioma, número de páginas e referências ({@code key}) para os autores.</li>
 *   <li><strong>Resolução dos autores</strong> — para cada referência de autor, faz
 *       {@code GET /authors/{id}.json}: retorna nome, bio, data de nascimento e data de falecimento.
 *       Falhas na busca de um autor individual são ignoradas silenciosamente para não impedir
 *       o retorno do livro.</li>
 * </ol>
 *
 * <p>O baseUrl é injetável para facilitar os testes com {@code MockWebServer}, sem expor esse
 * construtor na API pública (visibilidade package-private).
 *
 * <p>HTTP 404 é tratado como "não encontrado" (retorna {@code Optional.empty()}). Qualquer outro
 * status não-2xx lança {@link br.com.yuri.alpha7.domain.exception.OpenLibraryUnavailableException}.
 * Timeout e erros de I/O também resultam nessa exceção.
 */
public class OpenLibraryClientImpl implements OpenLibraryClient {

    private static final Logger logger = LoggerFactory.getLogger(OpenLibraryClientImpl.class);

    private static final String DEFAULT_BASE_URL = "https://openlibrary.org";
    private static final String ISBN_ENDPOINT = "/isbn/%s.json";
    private static final String AUTHOR_ENDPOINT = "/authors/%s.json";

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final OpenLibraryBookMapper mapper;
    private final String baseUrl;

    public OpenLibraryClientImpl(OkHttpClient httpClient, ObjectMapper objectMapper, OpenLibraryBookMapper mapper) {
        this(httpClient, objectMapper, mapper, DEFAULT_BASE_URL);
    }

    OpenLibraryClientImpl(OkHttpClient httpClient, ObjectMapper objectMapper, OpenLibraryBookMapper mapper, String baseUrl) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.mapper = mapper;
        this.baseUrl = baseUrl;
    }

    @Override
    public Optional<Livro> findByIsbn(ISBN isbn) {
        String url = buildIsbnUrl(isbn);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        logger.debug("Consultando OpenLibrary: GET {}", url);
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.code() == 404) {
                logger.debug("ISBN {} não encontrado na OpenLibrary (HTTP 404)", isbn.getValue());
                return Optional.empty();
            }
            if (!response.isSuccessful()) {
                logger.warn("OpenLibrary retornou status inesperado {} para ISBN {}", response.code(), isbn.getValue());
                throw new OpenLibraryUnavailableException(
                        "OpenLibrary indisponível. Código HTTP: " + response.code()
                );
            }

            OpenLibraryBookResponse bookResponse = objectMapper.readValue(
                    readBody(response), OpenLibraryBookResponse.class
            );
            List<OpenLibraryAuthorResponse> authors = resolveAuthors(bookResponse.getAuthors());
            logger.debug("ISBN {} encontrado na OpenLibrary: '{}', {} autor(es)",
                    isbn.getValue(), bookResponse.getTitle(), authors.size());

            return Optional.of(mapper.toLivro(bookResponse, authors, isbn));
        } catch (IOException e) {
            logger.warn("Falha de comunicação com OpenLibrary ao buscar ISBN {}: {}", isbn.getValue(), e.getMessage());
            throw new OpenLibraryUnavailableException(e.getMessage());
        }
    }

    private List<OpenLibraryAuthorResponse> resolveAuthors(List<OpenLibraryBookResponse.AuthorRef> authorRefs) {
        List<OpenLibraryAuthorResponse> authors = new ArrayList<>();
        for (OpenLibraryBookResponse.AuthorRef ref : authorRefs) {
            fetchAuthor(ref.getKey()).ifPresent(authors::add);
        }
        return authors;
    }

    private Optional<OpenLibraryAuthorResponse> fetchAuthor(String authorKey) {
        if (authorKey == null) {
            return Optional.empty();
        }
        String authorId = authorKey.substring(authorKey.lastIndexOf('/') + 1);
        logger.debug("Buscando dados do autor: {}", authorId);

        Request request = new Request.Builder()
                .url(buildAuthorUrl(authorId))
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.debug("Autor {} não disponível (HTTP {}) — ignorado", authorId, response.code());
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(readBody(response), OpenLibraryAuthorResponse.class));
        } catch (IOException e) {
            logger.debug("Falha ao buscar autor {}: {} — ignorado", authorId, e.getMessage());
            return Optional.empty();
        }
    }

    private String readBody(Response response) throws IOException {
        ResponseBody body = response.body();
        return body == null ? "" : body.string();
    }

    private String buildIsbnUrl(ISBN isbn) {
        return baseUrl + String.format(ISBN_ENDPOINT, isbn.getValue());
    }

    private String buildAuthorUrl(String authorId) {
        return baseUrl + String.format(AUTHOR_ENDPOINT, authorId);
    }
}
