package br.com.yuri.alpha7.application.stats;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
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

        Map.Entry<String, Long> autorEntry = new AbstractMap.SimpleEntry<>("Robert C. Martin", 3L);
        List<Map.Entry<String, Long>> topAutores  = Collections.singletonList(autorEntry);
        List<Map.Entry<String, Long>> topEditoras = Collections.emptyList();

        Map<Integer, Long> livrosPorAno = new HashMap<>();
        livrosPorAno.put(2020, 2L);

        AcervoStats stats = new AcervoStats(10, livrosPorIdioma, topAutores, topEditoras, livrosPorAno);

        assertEquals(10,             stats.getTotalLivros());
        assertEquals(livrosPorIdioma, stats.getLivrosPorIdioma());
        assertEquals(topAutores,      stats.getTopAutores());
        assertEquals(topEditoras,     stats.getTopEditoras());
        assertEquals(livrosPorAno,    stats.getLivrosPorAno());
    }
}
