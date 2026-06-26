package br.com.yuri.alpha7.infra.client.openlibrary.mapper;

import br.com.yuri.alpha7.domain.autor.model.Autor;
import br.com.yuri.alpha7.domain.editora.model.Editora;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import br.com.yuri.alpha7.infra.client.openlibrary.dto.OpenLibraryAuthorResponse;
import br.com.yuri.alpha7.infra.client.openlibrary.dto.OpenLibraryBookResponse;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class OpenLibraryBookMapper {

    private static final DateTimeFormatter[] PUBLISH_DATE_FORMATS = {
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMM d, yyyy",  Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMM dd, yyyy",  Locale.ENGLISH)
    };

    private static final DateTimeFormatter[] PUBLISH_MONTH_YEAR_FORMATS = {
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMM yyyy",  Locale.ENGLISH)
    };

    private static final DateTimeFormatter[] AUTHOR_DATE_FORMATS = {
            DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH),
            DateTimeFormatter.ISO_LOCAL_DATE
    };

    public Livro toLivro(OpenLibraryBookResponse response, List<OpenLibraryAuthorResponse> authors, ISBN isbn) {
        Livro livro = new Livro();
        livro.setIsbn(isbn);
        livro.setTitulo(response.getTitle());
        livro.setNumeroPaginas(response.getNumberOfPages());
        parsePublishDate(response.getPublishDate()).ifPresent(livro::setDataPublicacao);
        extractLanguage(response.getLanguages()).ifPresent(livro::setIdioma);
        livro.setAutores(toAutores(authors));
        if (!response.getPublishers().isEmpty()) {
            livro.setEditora(new Editora(response.getPublishers().get(0)));
        }
        return livro;
    }

    private List<Autor> toAutores(List<OpenLibraryAuthorResponse> authorResponses) {
        List<Autor> autores = new ArrayList<>();
        for (OpenLibraryAuthorResponse response : authorResponses) {
            if (response.getName() == null) continue;
            Autor autor = new Autor(response.getName());
            parseAuthorDate(response.getBirthDate()).ifPresent(autor::setDataNascimento);
            parseAuthorDate(response.getDeathDate()).ifPresent(autor::setDataFalecimento);
            extractBio(response.getBio()).ifPresent(autor::setBio);
            autores.add(autor);
        }
        return autores;
    }

    private Optional<LocalDate> parsePublishDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) {
            return Optional.empty();
        }
        String date = rawDate.trim();

        for (DateTimeFormatter formatter : PUBLISH_DATE_FORMATS) {
            try {
                return Optional.of(LocalDate.parse(date, formatter));
            } catch (DateTimeParseException ignored) {
            }
        }

        for (DateTimeFormatter formatter : PUBLISH_MONTH_YEAR_FORMATS) {
            try {
                return Optional.of(YearMonth.parse(date, formatter).atDay(1));
            } catch (DateTimeParseException ignored) {
            }
        }

        try {
            int year = Integer.parseInt(date);
            if (year >= 1000 && year <= 9999) {
                return Optional.of(LocalDate.of(year, 1, 1));
            }
        } catch (NumberFormatException ignored) {
        }

        return Optional.empty();
    }

    private Optional<LocalDate> parseAuthorDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) {
            return Optional.empty();
        }
        for (DateTimeFormatter formatter : AUTHOR_DATE_FORMATS) {
            try {
                return Optional.of(LocalDate.parse(rawDate.trim(), formatter));
            } catch (DateTimeParseException notMatched) {
                continue;
            }
        }
        return Optional.empty();
    }

    private Optional<String> extractBio(JsonNode bioNode) {
        if (bioNode == null) {
            return Optional.empty();
        }
        if (bioNode.isTextual()) {
            return Optional.of(bioNode.asText());
        }
        if (bioNode.has("value")) {
            return Optional.of(bioNode.get("value").asText());
        }
        return Optional.empty();
    }

    private Optional<String> extractLanguage(List<OpenLibraryBookResponse.LanguageRef> languages) {
        if (languages == null || languages.isEmpty()) {
            return Optional.empty();
        }
        String key = languages.get(0).getKey();
        if (key == null) {
            return Optional.empty();
        }
        return Optional.of(key.substring(key.lastIndexOf('/') + 1));
    }
}
