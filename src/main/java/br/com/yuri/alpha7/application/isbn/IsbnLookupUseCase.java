package br.com.yuri.alpha7.application.isbn;

import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.repository.LivroRepository;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Caso de uso para preenchimento automático do formulário a partir de um ISBN.
 *
 * <p>A busca segue uma estratégia de dois níveis:
 * <ol>
 *   <li><strong>Acervo local</strong> — se o ISBN já estiver cadastrado, retorna os dados
 *       diretamente do banco, sem nenhuma chamada HTTP.</li>
 *   <li><strong>OpenLibrary API</strong> — se o ISBN não for encontrado localmente, consulta
 *       a API pública. O resultado positivo é armazenado em cache (Ehcache) para evitar
 *       requisições repetidas ao mesmo ISBN.</li>
 * </ol>
 *
 * <p>Este caso de uso não persiste nenhum dado — ele apenas retorna um objeto {@link br.com.yuri.alpha7.domain.livro.model.Livro}
 * pré-preenchido para que o usuário revise e confirme antes de salvar.
 */
public class IsbnLookupUseCase {

    private static final Logger logger = LoggerFactory.getLogger(IsbnLookupUseCase.class);

    private final LivroRepository livroRepository;
    private final OpenLibraryClient openLibraryClient;

    public IsbnLookupUseCase(LivroRepository livroRepository, OpenLibraryClient openLibraryClient) {
        this.livroRepository = livroRepository;
        this.openLibraryClient = openLibraryClient;
    }

    /**
     * Busca um livro pelo ISBN, priorizando o acervo local.
     * Se não encontrado localmente, consulta a API da OpenLibrary.
     *
     * @param isbn ISBN do livro
     * @return {@link Optional} com o livro encontrado, ou vazio se não existir
     */
    public Optional<Livro> findByIsbn(ISBN isbn) {
        Optional<Livro> local = livroRepository.findByIsbn(isbn);
        if (local.isPresent()) {
            logger.debug("ISBN {} encontrado no acervo local: '{}'", isbn.getValue(), local.get().getTitulo());
            return local;
        }
        logger.debug("ISBN {} não encontrado localmente — consultando OpenLibrary", isbn.getValue());
        return openLibraryClient.findByIsbn(isbn);
    }
}
