package br.com.yuri.alpha7.application.stats;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class AcervoStats {
    private final long              totalLivros;
    private final Map<String, Long> livrosPorIdioma;
    private final List<Entry<String,Long>> topAutores;
    private final List<Entry<String,Long>> topEditoras;
    private final Map<Integer,Long> livrosPorAno;

    public AcervoStats(
            long totalLivros,
            Map<String, Long> livrosPorIdioma,
            List<Entry<String, Long>> topAutores,
            List<Entry<String, Long>> topEditoras,
            Map<Integer, Long> livrosPorAno
    ) {
        this.totalLivros    = totalLivros;
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

    public List<Entry<String, Long>> getTopAutores() {
        return topAutores;
    }

    public List<Entry<String, Long>> getTopEditoras() {
        return topEditoras;
    }

    public Map<Integer, Long> getLivrosPorAno() {
        return livrosPorAno;
    }
}
