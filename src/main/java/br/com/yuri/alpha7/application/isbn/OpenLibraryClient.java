package br.com.yuri.alpha7.application.isbn;

import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;

import java.util.Optional;

/**
 * Porta de saída para consulta de livros por ISBN em fontes externas.
 *
 * <p>Esta interface pertence à camada de aplicação — ela define o contrato que a aplicação
 * precisa, sem depender de detalhes de HTTP ou de qualquer serviço específico. A implementação
 * concreta ({@code OpenLibraryClientImpl}) e o decorator com cache ({@code CachingOpenLibraryClient})
 * ficam em {@code infra}, mantendo o domínio e a aplicação isolados de detalhes de rede.
 */
public interface OpenLibraryClient {

    /**
     * Busca metadados de um livro pelo ISBN em uma fonte externa.
     *
     * @param isbn ISBN do livro a consultar
     * @return {@link Optional} com o livro preenchido, ou vazio se o ISBN não for encontrado
     * @throws br.com.yuri.alpha7.domain.exception.OpenLibraryUnavailableException se o serviço
     *         externo estiver indisponível ou retornar um erro não esperado
     */
    Optional<Livro> findByIsbn(ISBN isbn);
}
