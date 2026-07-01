package br.com.yuri.alpha7.application.stats;

import br.com.yuri.alpha7.domain.autor.model.Autor;
import br.com.yuri.alpha7.domain.editora.model.Editora;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.repository.LivroRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcervoStatsUseCaseTest {

    @Mock
    LivroRepository repository;

    @InjectMocks
    AcervoStatsUseCase useCase;

    @Test
    @DisplayName(
            "Given an empty repository," +
            " when getAcervo is called," +
            " then all stats are zero or empty"
    )
    void shouldReturnZeroStatsWhenRepositoryIsEmpty() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        AcervoStats stats = useCase.getAcervo();

        assertEquals(0, stats.getTotalLivros());
        assertTrue(stats.getLivrosPorIdioma().isEmpty());
        assertTrue(stats.getTopAutores().isEmpty());
        assertTrue(stats.getTopEditoras().isEmpty());
        assertTrue(stats.getLivrosPorAno().isEmpty());
    }

    @Test
    @DisplayName(
            "Given books with different languages," +
            " when getAcervo is called," +
            " then livrosPorIdioma groups and counts them correctly"
    )
    void shouldGroupBooksByLanguage() {
        when(repository.findAll()).thenReturn(Arrays.asList(
                livroComIdioma("en"),
                livroComIdioma("en"),
                livroComIdioma("pt")
        ));

        AcervoStats stats = useCase.getAcervo();

        assertEquals(3,  stats.getTotalLivros());
        assertEquals(2L, stats.getLivrosPorIdioma().get("en"));
        assertEquals(1L, stats.getLivrosPorIdioma().get("pt"));
    }

    @Test
    @DisplayName(
            "Given books with null or blank language," +
            " when getAcervo is called," +
            " then both are counted under 'Não informado'"
    )
    void shouldUseNaoInformadoForNullOrBlankLanguage() {
        when(repository.findAll()).thenReturn(Arrays.asList(
                livroComIdioma(null),
                livroComIdioma("  ")
        ));

        AcervoStats stats = useCase.getAcervo();

        assertEquals(2L, stats.getLivrosPorIdioma().get("Não informado"));
    }

    @Test
    @DisplayName(
            "Given books with shared and unique authors," +
            " when getAcervo is called," +
            " then top authors are ordered by count descending"
    )
    void shouldRankAutoresByBookCountDescending() {
        Autor martin = new Autor("Robert C. Martin");
        Autor hunt   = new Autor("Andrew Hunt");

        when(repository.findAll()).thenReturn(Arrays.asList(
                livroComAutores(martin, hunt),
                livroComAutores(martin)
        ));

        AcervoStats stats = useCase.getAcervo();

        assertEquals("Robert C. Martin", stats.getTopAutores().get(0).getKey());
        assertEquals(2L,                 stats.getTopAutores().get(0).getValue());
        assertEquals("Andrew Hunt",      stats.getTopAutores().get(1).getKey());
        assertEquals(1L,                 stats.getTopAutores().get(1).getValue());
    }

    @Test
    @DisplayName(
            "Given more than five distinct authors in the repository," +
            " when getAcervo is called," +
            " then only the top five authors are returned"
    )
    void shouldLimitTopAutoresToFive() {
        List<Livro> livros = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            livros.add(livroComAutores(new Autor("Autor " + i)));
        }
        when(repository.findAll()).thenReturn(livros);

        AcervoStats stats = useCase.getAcervo();

        assertEquals(5, stats.getTopAutores().size());
    }

    @Test
    @DisplayName(
            "Given a book with a publisher and one without," +
            " when getAcervo is called," +
            " then null publisher is counted under 'Não informado'"
    )
    void shouldUseNaoInformadoForNullEditora() {
        Livro comEditora    = livroComIdioma("en");
        comEditora.setEditora(new Editora("Pearson"));

        Livro semEditora = livroComIdioma("en");
        semEditora.setEditora(null);

        when(repository.findAll()).thenReturn(Arrays.asList(comEditora, semEditora));

        AcervoStats stats = useCase.getAcervo();

        long pearsonCount       = stats.getTopEditoras().stream()
                .filter(e -> "Pearson".equals(e.getKey())).mapToLong(e -> e.getValue()).sum();
        long naoInformadoCount  = stats.getTopEditoras().stream()
                .filter(e -> "Não informado".equals(e.getKey())).mapToLong(e -> e.getValue()).sum();

        assertEquals(1L, pearsonCount);
        assertEquals(1L, naoInformadoCount);
    }

    @Test
    @DisplayName(
            "Given books with and without publication date," +
            " when getAcervo is called," +
            " then only books with date appear in livrosPorAno"
    )
    void shouldIncludeOnlyBooksWithDateInLivrosPorAno() {
        Livro comData = livroComIdioma("en");
        comData.setDataPublicacao(LocalDate.of(2020, 6, 1));

        Livro semData = livroComIdioma("pt");
        semData.setDataPublicacao(null);

        when(repository.findAll()).thenReturn(Arrays.asList(comData, semData));

        AcervoStats stats = useCase.getAcervo();

        assertEquals(1,  stats.getLivrosPorAno().size());
        assertEquals(1L, stats.getLivrosPorAno().get(2020));
    }

    @Test
    @DisplayName(
            "Given multiple books published in the same year," +
            " when getAcervo is called," +
            " then they are grouped under the same year entry"
    )
    void shouldGroupBooksPublishedInTheSameYear() {
        Livro livro1 = livroComIdioma("en");
        livro1.setDataPublicacao(LocalDate.of(2008, 1, 1));

        Livro livro2 = livroComIdioma("en");
        livro2.setDataPublicacao(LocalDate.of(2008, 8, 1));

        when(repository.findAll()).thenReturn(Arrays.asList(livro1, livro2));

        AcervoStats stats = useCase.getAcervo();

        assertEquals(1,  stats.getLivrosPorAno().size());
        assertEquals(2L, stats.getLivrosPorAno().get(2008));
    }

    private Livro livroComIdioma(String idioma) {
        Livro livro = new Livro();
        livro.setIdioma(idioma);
        livro.setAutores(Collections.emptyList());
        return livro;
    }

    private Livro livroComAutores(Autor... autores) {
        Livro livro = new Livro();
        livro.setAutores(Arrays.asList(autores));
        return livro;
    }
}
