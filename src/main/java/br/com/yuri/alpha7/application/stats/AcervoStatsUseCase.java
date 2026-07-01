package br.com.yuri.alpha7.application.stats;

import br.com.yuri.alpha7.domain.autor.model.Autor;
import br.com.yuri.alpha7.domain.editora.model.Editora;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.repository.LivroRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

public class AcervoStatsUseCase {

    private final LivroRepository repository;

    public AcervoStatsUseCase(LivroRepository repository) {
        this.repository = repository;
    }

    public AcervoStats getAcervo() {
        List<Livro> livros = repository.findAll();

        int totalLivros = livros.size();

        Map<String, Long> livrosPorIdioma = livros.stream()
                .collect(groupingBy(
                        livro -> valorOuNaoInformado(livro.getIdioma()),
                        counting()
                ));

        List<Map.Entry<String, Long>> topAutores = livros.stream()
                .flatMap(livro -> livro.getAutores().stream())
                .map(autor -> valorOuNaoInformado(nomeAutor(autor)))
                .collect(groupingBy(nome -> nome, counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .collect(java.util.stream.Collectors.toList());

        List<Map.Entry<String, Long>> topEditoras = livros.stream()
                .map(livro -> valorOuNaoInformado(nomeEditora(livro.getEditora())))
                .collect(groupingBy(nome -> nome, counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .collect(java.util.stream.Collectors.toList());

        Map<Integer, Long> livrosPorAno = livros.stream()
                .filter(livro -> livro.getDataPublicacao() != null)
                .collect(groupingBy(
                        livro -> livro.getDataPublicacao().getYear(),
                        counting()
                ));

        return new AcervoStats(
                totalLivros,
                livrosPorIdioma,
                topAutores,
                topEditoras,
                livrosPorAno
        );
    }

    private String nomeAutor(Autor autor) {
        if (autor == null) {
            return null;
        }

        return autor.getNome();
    }

    private String nomeEditora(Editora editora) {
        if (editora == null) {
            return null;
        }

        return editora.getNome();
    }

    private String valorOuNaoInformado(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return "Não informado";
        }

        return valor.trim();
    }
}