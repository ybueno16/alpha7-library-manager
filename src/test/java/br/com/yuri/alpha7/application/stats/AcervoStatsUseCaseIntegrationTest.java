package br.com.yuri.alpha7.application.stats;

import br.com.yuri.alpha7.domain.autor.model.Autor;
import br.com.yuri.alpha7.domain.editora.model.Editora;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import br.com.yuri.alpha7.infra.persistence.AbstractRepositoryTest;
import br.com.yuri.alpha7.infra.persistence.HibernateUnitOfWork;
import br.com.yuri.alpha7.infra.persistence.autor.AutorRepositoryImpl;
import br.com.yuri.alpha7.infra.persistence.editora.EditoraRepositoryImpl;
import br.com.yuri.alpha7.infra.persistence.livro.LivroRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class AcervoStatsUseCaseIntegrationTest extends AbstractRepositoryTest {

    private final LivroRepositoryImpl   livroRepository   = new LivroRepositoryImpl();
    private final AutorRepositoryImpl   autorRepository   = new AutorRepositoryImpl();
    private final EditoraRepositoryImpl editoraRepository = new EditoraRepositoryImpl();
    private final HibernateUnitOfWork   unitOfWork        = new HibernateUnitOfWork();
    private final AcervoStatsUseCase    useCase           = new AcervoStatsUseCase(livroRepository, unitOfWork);

    @Test
    @DisplayName(
            "Given books with distinct languages in the repository," +
            " when getAcervo is called," +
            " then livrosPorIdioma reflects counts from database"
    )
    void shouldAggregateStatsByIdiomaFromDatabase() {
        saveLivro("Book A", "9780132350884", "en", null, null);
        saveLivro("Book B", "9780134685991", "en", null, null);
        saveLivro("Book C", "9780201633610", "pt", null, null);

        AcervoStats stats = useCase.getAcervo();

        assertEquals(3L, stats.getTotalLivros());
        assertEquals(2L, stats.getLivrosPorIdioma().get("en"));
        assertEquals(1L, stats.getLivrosPorIdioma().get("pt"));
    }

    @Test
    @DisplayName(
            "Given books with authors in the repository," +
            " when getAcervo is called," +
            " then topAutores reflects author counts from database"
    )
    void shouldAggregateTopAutoresFromDatabase() {
        Autor martin = autorRepository.save(new Autor("Robert C. Martin"));
        Autor hunt   = autorRepository.save(new Autor("Andrew Hunt"));

        saveLivroComAutores("Clean Code",               "9780132350884", martin, hunt);
        saveLivroComAutores("The Pragmatic Programmer", "9780134685991", martin);

        AcervoStats stats = useCase.getAcervo();

        assertEquals("Robert C. Martin", stats.getTopAutores().get(0).getNome());
        assertEquals(2L,                 stats.getTopAutores().get(0).getTotal());
    }

    @Test
    @DisplayName(
            "Given books with and without editora," +
            " when getAcervo is called," +
            " then null editora is grouped under 'Não informado'"
    )
    void shouldGroupNullEditoraAsNaoInformado() {
        Editora editora = editoraRepository.save(new Editora("Prentice Hall"));

        Livro comEditora = livroSemAssociacoes("Book A", "9780132350884");
        comEditora.setEditora(editora);
        livroRepository.save(comEditora);

        livroRepository.save(livroSemAssociacoes("Book B", "9780134685991"));

        AcervoStats stats = useCase.getAcervo();

        long pearson      = stats.getTopEditoras().stream()
                .filter(e -> "Prentice Hall".equals(e.getNome())).mapToLong(StatEntry::getTotal).sum();
        long naoInformado = stats.getTopEditoras().stream()
                .filter(e -> "Não informado".equals(e.getNome())).mapToLong(StatEntry::getTotal).sum();

        assertEquals(1L, pearson);
        assertEquals(1L, naoInformado);
    }

    @Test
    @DisplayName(
            "Given books with different publication years," +
            " when getAcervo is called," +
            " then livrosPorAno groups by year in ascending order"
    )
    void shouldAggregateLivrosPorAnoInAscendingOrder() {
        saveLivro("Book 2020a", "9780132350884", null, LocalDate.of(2020, 9, 1), null);
        saveLivro("Book 2008",  "9780134685991", null, LocalDate.of(2008, 1, 1), null);
        saveLivro("Book 2020b", "9780201633610", null, LocalDate.of(2020, 6, 15), null);

        AcervoStats stats = useCase.getAcervo();

        assertEquals(2,  stats.getLivrosPorAno().size());
        assertEquals(1L, stats.getLivrosPorAno().get(2008));
        assertEquals(2L, stats.getLivrosPorAno().get(2020));
        assertEquals(2008, stats.getLivrosPorAno().keySet().iterator().next().intValue());
    }

    private void saveLivro(String titulo, String isbn, String idioma, LocalDate data, Editora editora) {
        Livro livro = livroSemAssociacoes(titulo, isbn);
        livro.setIdioma(idioma);
        livro.setDataPublicacao(data);
        livro.setEditora(editora);
        livroRepository.save(livro);
    }

    private void saveLivroComAutores(String titulo, String isbn, Autor... autores) {
        Livro livro = livroSemAssociacoes(titulo, isbn);
        livro.setAutores(Arrays.asList(autores));
        livroRepository.save(livro);
    }

    private Livro livroSemAssociacoes(String titulo, String isbn) {
        Livro livro = new Livro();
        livro.setTitulo(titulo);
        livro.setIsbn(new ISBN(isbn));
        livro.setAutores(Collections.emptyList());
        return livro;
    }
}
