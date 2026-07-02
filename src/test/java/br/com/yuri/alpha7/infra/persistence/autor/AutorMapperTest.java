package br.com.yuri.alpha7.infra.persistence.autor;

import br.com.yuri.alpha7.domain.autor.model.Autor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class AutorMapperTest {

    @Test
    @DisplayName(
            "Given a null entity," +
            " when toDomain is called," +
            " then null is returned"
    )
    void shouldReturnNullWhenEntityIsNull() {
        assertNull(AutorMapper.toDomain(null));
    }

    @Test
    @DisplayName(
            "Given a null domain object," +
            " when toEntity is called," +
            " then null is returned"
    )
    void shouldReturnNullWhenDomainIsNull() {
        assertNull(AutorMapper.toEntity(null));
    }
}
