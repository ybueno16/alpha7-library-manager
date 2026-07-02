package br.com.yuri.alpha7.domain.livro.repository;

import java.util.List;

/**
 * Resultado paginado retornado pelas consultas de listagem.
 *
 * <p>Armazena a página corrente de itens e o total de registros que atendem
 * aos filtros, permitindo que a camada de apresentação calcule navegação,
 * contadores e quantidade de páginas sem executar uma segunda regra de negócio.</p>
 *
 * @param <T> tipo dos itens paginados
 */
public final class PagedResult<T> {

    private final List<T> items;
    private final long    totalCount;

    /**
     * Cria um resultado paginado.
     *
     * @param items itens da página solicitada
     * @param totalCount total de registros existentes para o mesmo filtro
     */
    public PagedResult(List<T> items, long totalCount) {
        this.items      = items;
        this.totalCount = totalCount;
    }

    public List<T> getItems() {
        return items;
    }

    public long getTotalCount() {
        return totalCount;
    }

    /**
     * Calcula o total de páginas para um tamanho de página informado.
     *
     * @param pageSize quantidade de itens por página
     * @return total de páginas; retorna {@code 1} quando o tamanho é inválido
     */
    public int totalPages(int pageSize) {
        if (pageSize <= 0) return 1;
        return (int) Math.ceil((double) totalCount / pageSize);
    }
}
