package br.com.yuri.alpha7.application.stats;

import br.com.yuri.alpha7.application.UnitOfWork;
import br.com.yuri.alpha7.domain.livro.repository.LivroRepository;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AcervoStatsUseCase {

    private final LivroRepository repository;
    private final UnitOfWork      unitOfWork;

    public AcervoStatsUseCase(LivroRepository repository, UnitOfWork unitOfWork) {
        this.repository = repository;
        this.unitOfWork = unitOfWork;
    }

    public AcervoStats getAcervo() {
        return unitOfWork.execute(() -> {
            long total = repository.countAll();

            Map<String, Long> livrosPorIdioma = new HashMap<>();
            repository.countByIdioma().forEach((idioma, count) ->
                    livrosPorIdioma.merge(valorOuNaoInformado(idioma), count, Long::sum));

            Map<String, Long> editoraAgrupada = new HashMap<>();
            repository.countByEditora().forEach((nome, count) ->
                    editoraAgrupada.merge(valorOuNaoInformado(nome), count, Long::sum));

            List<Map.Entry<String, Long>> topAutores = repository.countByAutor()
                    .entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                    .limit(5)
                    .collect(Collectors.toList());

            List<Map.Entry<String, Long>> topEditoras = editoraAgrupada.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                    .limit(5)
                    .collect(Collectors.toList());

            Map<Integer, Long> livrosPorAno = repository.countByAno();

            return new AcervoStats(total, livrosPorIdioma, topAutores, topEditoras, livrosPorAno);
        });
    }

    private String valorOuNaoInformado(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return "Não informado";
        }
        return valor.trim();
    }
}
