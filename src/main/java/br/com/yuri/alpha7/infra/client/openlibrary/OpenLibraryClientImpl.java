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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OpenLibraryClientImpl implements OpenLibraryClient {

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
        Request request = new Request.Builder()
                .url(buildIsbnUrl(isbn))
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.code() == 404) {
                return Optional.empty();
            }
            if (!response.isSuccessful()) {
                throw new OpenLibraryUnavailableException(
                        "Open Library API is unavailable. Response code: " + response.code()
                );
            }

            OpenLibraryBookResponse bookResponse = objectMapper.readValue(
                    readBody(response), OpenLibraryBookResponse.class
            );
            List<OpenLibraryAuthorResponse> authors = resolveAuthors(bookResponse.getAuthors());

            return Optional.of(mapper.toLivro(bookResponse, authors, isbn));
        } catch (IOException e) {
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
        Request request = new Request.Builder()
                .url(buildAuthorUrl(authorId))
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(readBody(response), OpenLibraryAuthorResponse.class));
        } catch (IOException e) {
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
