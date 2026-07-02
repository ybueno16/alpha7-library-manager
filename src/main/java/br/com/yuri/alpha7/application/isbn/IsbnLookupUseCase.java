package br.com.yuri.alpha7.application.isbn;

import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Caso de uso para preenchimento automático do formulário a partir de um ISBN.
 * Consulta o cache (Ehcache) e, se ausente, a API da OpenLibrary.
 * A checagem de duplicatas no acervo local é responsabilidade do repositório.
 */
public class IsbnLookupUseCase {

    private static final Logger logger = LoggerFactory.getLogger(IsbnLookupUseCase.class);

    private final OpenLibraryClient openLibraryClient;

    public IsbnLookupUseCase(OpenLibraryClient openLibraryClient) {
        this.openLibraryClient = openLibraryClient;
    }

    /**
     * Busca um livro pelo ISBN consultando o cache e, se ausente, a API da OpenLibrary.
     * O acervo local não é consultado aqui — a checagem de duplicatas é feita
     * diretamente pelo repositório no preview de importação.
     *
     * @param isbn ISBN do livro
     * @return {@link Optional} com o livro encontrado, ou vazio se não existir
     */
    public Optional<Livro> findByIsbn(ISBN isbn) {
        logger.debug("Buscando ISBN {} na OpenLibrary (cache ou API)", isbn.getValue());
        return openLibraryClient.findByIsbn(isbn);
    }
}
