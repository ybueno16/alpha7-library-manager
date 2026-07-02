package br.com.yuri.alpha7.application.stats;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AcervoStatsTest {

    @Test
    @DisplayName(
            "Given all fields provided to the constructor," +
            " when getters are called," +
            " then each returns the corresponding value"
    )
    void shouldReturnAllFieldsViaGetters() {
        Map<String, Long> livrosPorIdioma = new HashMap<>();
        livrosPorIdioma.put("en", 5L);

        StatEntry autorEntry = new StatEntry("Robert C. Martin", 3L);
        List<StatEntry> topAutores  = Collections.singletonList(autorEntry);
        List<StatEntry> topEditoras = Collections.emptyList();

        Map<Integer, Long> livrosPorAno = new HashMap<>();
        livrosPorAno.put(2020, 2L);

        AcervoStats stats = new AcervoStats(10, livrosPorIdioma, topAutores, topEditoras, livrosPorAno);

        assertEquals(10,             stats.getTotalLivros());
        assertEquals(livrosPorIdioma, stats.getLivrosPorIdioma());
        assertEquals(topAutores,      stats.getTopAutores());
        assertEquals(topEditoras,     stats.getTopEditoras());
        assertEquals(livrosPorAno,    stats.getLivrosPorAno());
    }

    @Test
    @DisplayName(
            "Given a StatEntry," +
            " when getters are called," +
            " then nome and total are returned"
    )
    void shouldExposeNomeAndTotalViaStatEntry() {
        StatEntry entry = new StatEntry("Clean Code", 7L);
        assertEquals("Clean Code", entry.getNome());
        assertEquals(7L, entry.getTotal());
    }
}
