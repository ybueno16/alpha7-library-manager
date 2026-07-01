package br.com.yuri.alpha7.application.stats;

import br.com.yuri.alpha7.application.UnitOfWork;
import br.com.yuri.alpha7.domain.livro.repository.LivroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcervoStatsUseCaseTest {

    @Mock
    LivroRepository repository;

    @Mock
    UnitOfWork unitOfWork;

    @InjectMocks
    AcervoStatsUseCase useCase;

    @BeforeEach
    void setupUnitOfWork() {
        doAnswer(inv -> ((Supplier<?>) inv.getArgument(0)).get())
                .when(unitOfWork).execute(any(Supplier.class));
    }

    @Test
    @DisplayName(
            "Given an empty repository," +
            " when getAcervo is called," +
            " then all stats are zero or empty"
    )
    void shouldReturnZeroStatsWhenRepositoryIsEmpty() {
        when(repository.countAll()).thenReturn(0L);
        when(repository.countByIdioma()).thenReturn(Collections.emptyMap());
        when(repository.countByAutor()).thenReturn(Collections.emptyMap());
        when(repository.countByEditora()).thenReturn(Collections.emptyMap());
        when(repository.countByAno()).thenReturn(Collections.emptyMap());

        AcervoStats stats = useCase.getAcervo();

        assertEquals(0L, stats.getTotalLivros());
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
        Map<String, Long> idiomaMap = new HashMap<>();
        idiomaMap.put("en", 2L);
        idiomaMap.put("pt", 1L);

        when(repository.countAll()).thenReturn(3L);
        when(repository.countByIdioma()).thenReturn(idiomaMap);
        when(repository.countByAutor()).thenReturn(Collections.emptyMap());
        when(repository.countByEditora()).thenReturn(Collections.emptyMap());
        when(repository.countByAno()).thenReturn(Collections.emptyMap());

        AcervoStats stats = useCase.getAcervo();

        assertEquals(3L,  stats.getTotalLivros());
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
        Map<String, Long> idiomaMap = new HashMap<>();
        idiomaMap.put(null, 1L);
        idiomaMap.put("  ", 1L);

        when(repository.countAll()).thenReturn(2L);
        when(repository.countByIdioma()).thenReturn(idiomaMap);
        when(repository.countByAutor()).thenReturn(Collections.emptyMap());
        when(repository.countByEditora()).thenReturn(Collections.emptyMap());
        when(repository.countByAno()).thenReturn(Collections.emptyMap());

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
        Map<String, Long> autorMap = new HashMap<>();
        autorMap.put("Robert C. Martin", 2L);
        autorMap.put("Andrew Hunt", 1L);

        when(repository.countAll()).thenReturn(2L);
        when(repository.countByIdioma()).thenReturn(Collections.emptyMap());
        when(repository.countByAutor()).thenReturn(autorMap);
        when(repository.countByEditora()).thenReturn(Collections.emptyMap());
        when(repository.countByAno()).thenReturn(Collections.emptyMap());

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
        Map<String, Long> autorMap = new HashMap<>();
        for (int i = 1; i <= 7; i++) {
            autorMap.put("Autor " + i, (long) i);
        }

        when(repository.countAll()).thenReturn(7L);
        when(repository.countByIdioma()).thenReturn(Collections.emptyMap());
        when(repository.countByAutor()).thenReturn(autorMap);
        when(repository.countByEditora()).thenReturn(Collections.emptyMap());
        when(repository.countByAno()).thenReturn(Collections.emptyMap());

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
        Map<String, Long> editoraMap = new HashMap<>();
        editoraMap.put("Pearson", 1L);
        editoraMap.put(null, 1L);

        when(repository.countAll()).thenReturn(2L);
        when(repository.countByIdioma()).thenReturn(Collections.emptyMap());
        when(repository.countByAutor()).thenReturn(Collections.emptyMap());
        when(repository.countByEditora()).thenReturn(editoraMap);
        when(repository.countByAno()).thenReturn(Collections.emptyMap());

        AcervoStats stats = useCase.getAcervo();

        long pearsonCount      = stats.getTopEditoras().stream()
                .filter(e -> "Pearson".equals(e.getKey())).mapToLong(Map.Entry::getValue).sum();
        long naoInformadoCount = stats.getTopEditoras().stream()
                .filter(e -> "Não informado".equals(e.getKey())).mapToLong(Map.Entry::getValue).sum();

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
        Map<Integer, Long> anoMap = new TreeMap<>();
        anoMap.put(2020, 1L);

        when(repository.countAll()).thenReturn(2L);
        when(repository.countByIdioma()).thenReturn(Collections.emptyMap());
        when(repository.countByAutor()).thenReturn(Collections.emptyMap());
        when(repository.countByEditora()).thenReturn(Collections.emptyMap());
        when(repository.countByAno()).thenReturn(anoMap);

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
        Map<Integer, Long> anoMap = new TreeMap<>();
        anoMap.put(2008, 2L);

        when(repository.countAll()).thenReturn(2L);
        when(repository.countByIdioma()).thenReturn(Collections.emptyMap());
        when(repository.countByAutor()).thenReturn(Collections.emptyMap());
        when(repository.countByEditora()).thenReturn(Collections.emptyMap());
        when(repository.countByAno()).thenReturn(anoMap);

        AcervoStats stats = useCase.getAcervo();

        assertEquals(1,  stats.getLivrosPorAno().size());
        assertEquals(2L, stats.getLivrosPorAno().get(2008));
    }
}
