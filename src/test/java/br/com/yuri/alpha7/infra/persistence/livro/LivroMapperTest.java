package br.com.yuri.alpha7.infra.persistence.livro;

import br.com.yuri.alpha7.domain.autor.model.Autor;
import br.com.yuri.alpha7.domain.editora.model.Editora;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import br.com.yuri.alpha7.infra.persistence.autor.AutorEntity;
import br.com.yuri.alpha7.infra.persistence.editora.EditoraEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LivroMapperTest {

    private static final ISBN ISBN_VALIDO = new ISBN("9780132350884");

    @Test
    @DisplayName(
            "Given a null entity," +
            " when toDomain is called," +
            " then null is returned"
    )
    void shouldReturnNullWhenEntityIsNull() {
        assertNull(LivroMapper.toDomain(null));
    }

    @Test
    @DisplayName(
            "Given a null domain object," +
            " when toEntity is called," +
            " then null is returned"
    )
    void shouldReturnNullWhenDomainIsNull() {
        assertNull(LivroMapper.toEntity(null));
    }

    @Test
    @DisplayName(
            "Given an entity without editora and with one autor," +
            " when toDomain is called," +
            " then scalar fields, editora and autores are mapped correctly"
    )
    void shouldMapEntityFieldsToDomain() {
        AutorEntity autor = new AutorEntity();
        autor.setId(1L);
        autor.setNome("Robert C. Martin");

        LivroEntity entity = new LivroEntity();
        entity.setId(10L);
        entity.setTitulo("Clean Code");
        entity.setIsbn(ISBN_VALIDO);
        entity.setEditora(null);
        entity.setAutores(Collections.singletonList(autor));

        Livro livro = LivroMapper.toDomain(entity);

        assertEquals(10L, livro.getId());
        assertEquals("Clean Code", livro.getTitulo());
        assertEquals(ISBN_VALIDO, livro.getIsbn());
        assertNull(livro.getEditora());
        assertEquals(1, livro.getAutores().size());
        assertEquals("Robert C. Martin", livro.getAutores().get(0).getNome());
    }

    @Test
    @DisplayName(
            "Given an entity with livrosSemelhantes populated," +
            " when toDomain is called," +
            " then livrosSemelhantes is always empty in the resulting domain object"
    )
    void shouldIgnoreSemelhantesOnPlainToDomain() {
        LivroEntity semelhante = new LivroEntity();
        semelhante.setId(20L);
        semelhante.setTitulo("The Pragmatic Programmer");
        semelhante.setIsbn(ISBN_VALIDO);

        LivroEntity entity = new LivroEntity();
        entity.setId(10L);
        entity.setTitulo("Clean Code");
        entity.setIsbn(ISBN_VALIDO);
        entity.setLivrosSemelhantes(Collections.singletonList(semelhante));

        Livro livro = LivroMapper.toDomain(entity);

        assertTrue(livro.getLivrosSemelhantes().isEmpty());
    }

    @Test
    @DisplayName(
            "Given an entity with one active and one soft-deleted semelhante," +
            " when toDomainWithSemelhantes is called," +
            " then only the active semelhante is included and without recursive semelhantes"
    )
    void shouldMapOnlyActiveSemelhantesWithoutRecursion() {
        LivroEntity semelhanteAtivo = new LivroEntity();
        semelhanteAtivo.setId(20L);
        semelhanteAtivo.setTitulo("The Pragmatic Programmer");
        semelhanteAtivo.setIsbn(ISBN_VALIDO);

        LivroEntity semelhanteDeletado = new LivroEntity();
        semelhanteDeletado.setId(30L);
        semelhanteDeletado.setTitulo("Refactoring");
        semelhanteDeletado.setIsbn(ISBN_VALIDO);
        semelhanteDeletado.setDeletedAt(LocalDateTime.now());

        LivroEntity entity = new LivroEntity();
        entity.setId(10L);
        entity.setTitulo("Clean Code");
        entity.setIsbn(ISBN_VALIDO);
        entity.setLivrosSemelhantes(Arrays.asList(semelhanteAtivo, semelhanteDeletado));

        Livro livro = LivroMapper.toDomainWithSemelhantes(entity);

        assertEquals(1, livro.getLivrosSemelhantes().size());
        Livro semelhanteMapeado = livro.getLivrosSemelhantes().get(0);
        assertEquals(20L, semelhanteMapeado.getId());
        assertTrue(semelhanteMapeado.getLivrosSemelhantes().isEmpty());
    }

    @Test
    @DisplayName(
            "Given a domain object with editora, autores and livrosSemelhantes," +
            " when toEntity is called," +
            " then all associations are mapped recursively"
    )
    void shouldMapDomainFieldsToEntity() {
        Editora editora = new Editora("Prentice Hall");
        editora.setId(1L);

        Autor autor = new Autor("Robert C. Martin");
        autor.setId(2L);

        Livro semelhante = new Livro();
        semelhante.setId(20L);
        semelhante.setTitulo("The Pragmatic Programmer");
        semelhante.setIsbn(ISBN_VALIDO);

        Livro livro = new Livro();
        livro.setId(10L);
        livro.setTitulo("Clean Code");
        livro.setIsbn(ISBN_VALIDO);
        livro.setEditora(editora);
        livro.setAutores(Collections.singletonList(autor));
        livro.setLivrosSemelhantes(Collections.singletonList(semelhante));

        LivroEntity entity = LivroMapper.toEntity(livro);

        assertEquals(10L, entity.getId());
        assertEquals("Clean Code", entity.getTitulo());
        assertEquals("Prentice Hall", entity.getEditora().getNome());
        assertEquals(1, entity.getAutores().size());
        assertEquals("Robert C. Martin", entity.getAutores().get(0).getNome());
        assertEquals(1, entity.getLivrosSemelhantes().size());
        assertEquals(20L, entity.getLivrosSemelhantes().get(0).getId());
    }
}
