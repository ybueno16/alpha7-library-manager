package br.com.yuri.alpha7.infra.persistence.editora;

import br.com.yuri.alpha7.domain.editora.model.Editora;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EditoraMapperTest {

    @Test
    @DisplayName(
            "Given a null entity," +
            " when toDomain is called," +
            " then null is returned"
    )
    void shouldReturnNullWhenEntityIsNull() {
        assertNull(EditoraMapper.toDomain(null));
    }

    @Test
    @DisplayName(
            "Given a null domain object," +
            " when toEntity is called," +
            " then null is returned"
    )
    void shouldReturnNullWhenDomainIsNull() {
        assertNull(EditoraMapper.toEntity(null));
    }

    @Test
    @DisplayName(
            "Given an entity with id and nome," +
            " when toDomain is called," +
            " then both fields are copied to the domain object"
    )
    void shouldMapEntityFieldsToDomain() {
        EditoraEntity entity = new EditoraEntity();
        entity.setId(1L);
        entity.setNome("Prentice Hall");

        Editora editora = EditoraMapper.toDomain(entity);

        assertEquals(1L, editora.getId());
        assertEquals("Prentice Hall", editora.getNome());
    }

    @Test
    @DisplayName(
            "Given a domain object with id and nome," +
            " when toEntity is called," +
            " then both fields are copied to the entity"
    )
    void shouldMapDomainFieldsToEntity() {
        Editora editora = new Editora("Addison-Wesley");
        editora.setId(2L);

        EditoraEntity entity = EditoraMapper.toEntity(editora);

        assertEquals(2L, entity.getId());
        assertEquals("Addison-Wesley", entity.getNome());
    }
}
