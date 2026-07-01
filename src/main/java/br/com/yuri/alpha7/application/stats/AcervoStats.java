package br.com.yuri.alpha7.application.stats;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class AcervoStats {
    private final int totalLivros;
    private final Map<String, Long> livrosPorIdioma;
    private final List<Entry<String,Long>> topAutores;
    private final List<Entry<String,Long>> topEditoras;
    private final Map<Integer,Long> livrosPorAno;

    public AcervoStats(
            int totalLivros,
            Map<String, Long> livrosPorIdioma,
            List<Entry<String, Long>> topAutores,
            List<Entry<String, Long>> topEditoras,
            Map<Integer, Long> livrosPorAno
    ) {
        this.totalLivros = totalLivros;
        this.livrosPorIdioma = livrosPorIdioma;
        this.topAutores = topAutores;
        this.topEditoras = topEditoras;
        this.livrosPorAno = livrosPorAno;
    }

    public int getTotalLivros() {
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
