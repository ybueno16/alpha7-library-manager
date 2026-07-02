package br.com.yuri.alpha7.application.stats;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Snapshot imutável das estatísticas exibidas no painel de acervo.
 *
 * <p>O objeto agrupa contadores calculados a partir dos livros ativos:
 * total geral, distribuição por idioma, ranking de autores e editoras e
 * distribuição por ano de publicação. As coleções expostas são somente leitura
 * para que a UI não altere acidentalmente o resultado calculado pelo caso de uso.</p>
 */
public class AcervoStats {
    private final long              totalLivros;
    private final Map<String, Long> livrosPorIdioma;
    private final List<StatEntry>   topAutores;
    private final List<StatEntry>   topEditoras;
    private final Map<Integer,Long> livrosPorAno;

    /**
     * Cria um snapshot de estatísticas do acervo.
     *
     * @param totalLivros total de livros ativos
     * @param livrosPorIdioma quantidade de livros por idioma
     * @param topAutores autores com maior quantidade de livros
     * @param topEditoras editoras com maior quantidade de livros
     * @param livrosPorAno quantidade de livros por ano de publicação
     */
    public AcervoStats(
            long totalLivros,
            Map<String, Long> livrosPorIdioma,
            List<StatEntry> topAutores,
            List<StatEntry> topEditoras,
            Map<Integer, Long> livrosPorAno
    ) {
        this.totalLivros     = totalLivros;
        this.livrosPorIdioma = Collections.unmodifiableMap(livrosPorIdioma);
        this.topAutores      = Collections.unmodifiableList(topAutores);
        this.topEditoras     = Collections.unmodifiableList(topEditoras);
        this.livrosPorAno    = Collections.unmodifiableMap(livrosPorAno);
    }

    public long getTotalLivros() {
        return totalLivros;
    }

    public Map<String, Long> getLivrosPorIdioma() {
        return livrosPorIdioma;
    }

    public List<StatEntry> getTopAutores() {
        return topAutores;
    }

    public List<StatEntry> getTopEditoras() {
        return topEditoras;
    }

    public Map<Integer, Long> getLivrosPorAno() {
        return livrosPorAno;
    }
}
