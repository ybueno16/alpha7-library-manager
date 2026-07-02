package br.com.yuri.alpha7.application.stats;

import br.com.yuri.alpha7.application.UnitOfWork;
import br.com.yuri.alpha7.domain.livro.repository.LivroRepository;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Caso de uso responsável por consolidar indicadores do acervo.
 *
 * <p>As consultas são executadas dentro de uma unidade de trabalho para manter
 * a mesma sessão de persistência durante a leitura. O resultado fica em cache
 * por um curto período para evitar consultas repetidas quando o usuário alterna
 * entre abas ou pressiona atualizar em sequência.</p>
 */
public class AcervoStatsUseCase {

    private static final long CACHE_TTL_MS = 30_000;

    private final LivroRepository repository;
    private final UnitOfWork      unitOfWork;

    private volatile AcervoStats cachedStats;
    private volatile long        cacheTimestamp = 0;

    public AcervoStatsUseCase(LivroRepository repository, UnitOfWork unitOfWork) {
        this.repository = repository;
        this.unitOfWork = unitOfWork;
    }

    /**
     * Retorna o snapshot atual de estatísticas do acervo.
     *
     * @return estatísticas agregadas dos livros ativos
     */
    public AcervoStats getAcervo() {
        long now = System.currentTimeMillis();
        if (cachedStats != null && (now - cacheTimestamp) < CACHE_TTL_MS) {
            return cachedStats;
        }
        AcervoStats fresh = unitOfWork.execute(() -> {
            long total = repository.countAll();

            Map<String, Long> livrosPorIdioma = new HashMap<>();
            repository.countByIdioma().forEach((idioma, count) ->
                    livrosPorIdioma.merge(valorOuNaoInformado(idioma), count, Long::sum));

            Map<String, Long> editoraAgrupada = new HashMap<>();
            repository.countByEditora().forEach((nome, count) ->
                    editoraAgrupada.merge(valorOuNaoInformado(nome), count, Long::sum));

            List<StatEntry> topAutores = repository.countByAutor()
                    .entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                            .thenComparing(Map.Entry.comparingByKey()))
                    .limit(5)
                    .map(e -> new StatEntry(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());

            List<StatEntry> topEditoras = editoraAgrupada.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                            .thenComparing(Map.Entry.comparingByKey()))
                    .limit(5)
                    .map(e -> new StatEntry(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());

            Map<Integer, Long> livrosPorAno = repository.countByAno();

            return new AcervoStats(total, livrosPorIdioma, topAutores, topEditoras, livrosPorAno);
        });
        cachedStats = fresh;
        cacheTimestamp = now;
        return fresh;
    }

    /**
     * Substitui valores nulos ou em branco por um rótulo padrão, usado para agrupar livros
     * sem idioma ou editora numa categoria visível em vez de aparecerem como chave nula.
     *
     * @param valor valor original do agrupamento, possivelmente nulo ou vazio
     * @return o próprio valor sem espaços nas bordas, ou {@code "Não informado"} se estiver vazio
     */
    private String valorOuNaoInformado(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return "Não informado";
        }
        return valor.trim();
    }
}
