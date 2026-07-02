package br.com.yuri.alpha7.application.stats;

/**
 * Par nome/contagem retornado pelo ranking de autores e editoras em {@link AcervoStats}.
 */
public final class StatEntry {

    private final String nome;
    private final long   total;

    public StatEntry(String nome, long total) {
        this.nome  = nome;
        this.total = total;
    }

    public String getNome() {
        return nome;
    }

    public long getTotal() {
        return total;
    }
}
