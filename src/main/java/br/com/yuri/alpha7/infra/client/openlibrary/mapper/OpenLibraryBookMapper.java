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

/**
 * Converte respostas da API da OpenLibrary em objetos de domínio.
 *
 * <p>A OpenLibrary não segue um padrão único de data: o campo {@code publish_date} pode vir como
 * ano ({@code "2003"}), mês e ano ({@code "August 2003"}), data completa em inglês
 * ({@code "August 1, 2003"}) ou ISO-8601 ({@code "2003-08-01"}). Esta classe tenta cada um
 * dos formatos conhecidos em sequência e ignora o campo se nenhum coincidir.
 *
 * <p>O idioma é extraído da referência ({@code /languages/eng → "eng"}); a editora é o primeiro
 * elemento da lista {@code publishers}. A bio do autor pode vir como string simples ou como
 * objeto JSON com campo {@code value} — ambos os casos são tratados.
 */
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

    /**
     * Converte a resposta da OpenLibrary (já com os autores resolvidos) para um {@link Livro}
     * de domínio. Campos que não puderem ser interpretados (data em formato desconhecido,
     * idioma ou editora ausentes) são simplesmente deixados de fora do livro resultante.
     *
     * @param response resposta do endpoint {@code /isbn/{isbn}.json}
     * @param authors  respostas já resolvidas do endpoint {@code /authors/{id}.json}
     * @param isbn     ISBN consultado, atribuído diretamente ao livro
     * @return livro preenchido com os dados disponíveis
     */
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

    /**
     * Converte as respostas de autor em objetos de domínio, ignorando entradas sem nome
     * (a OpenLibrary pode retornar referências que não resolveram para um nome válido).
     *
     * @param authorResponses respostas já resolvidas do endpoint de autores
     * @return autores convertidos, na mesma ordem em que foram resolvidos
     */
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

    /**
     * Tenta interpretar a data de publicação nos formatos conhecidos da OpenLibrary, em ordem:
     * data completa ({@link #PUBLISH_DATE_FORMATS}), mês e ano ({@link #PUBLISH_MONTH_YEAR_FORMATS},
     * assumindo o dia 1), e por fim ano isolado (assumindo 1º de janeiro).
     *
     * @param rawDate valor bruto do campo {@code publish_date}, possivelmente nulo ou vazio
     * @return data interpretada, ou vazio se nenhum formato conhecido coincidir
     */
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

    /**
     * Tenta interpretar uma data de nascimento ou falecimento de autor nos formatos conhecidos
     * da OpenLibrary ({@link #AUTHOR_DATE_FORMATS}), em ordem.
     *
     * @param rawDate valor bruto do campo de data, possivelmente nulo ou vazio
     * @return data interpretada, ou vazio se nenhum formato conhecido coincidir
     */
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

    /**
     * Extrai o texto da bio do autor, que a OpenLibrary retorna ora como string simples,
     * ora como objeto JSON com um campo {@code value}.
     *
     * @param bioNode nó JSON do campo {@code bio}, possivelmente nulo
     * @return texto da bio, ou vazio se o nó for nulo ou não contiver texto reconhecível
     */
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

    /**
     * Extrai o código do idioma a partir da primeira referência da lista, cujo formato é
     * {@code /languages/eng} — apenas a parte após a última barra é retida.
     *
     * @param languages referências de idioma da resposta, possivelmente nula ou vazia
     * @return código do idioma (ex: {@code "eng"}), ou vazio se não houver referência válida
     */
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
