package br.com.yuri.alpha7.domain;

import br.com.yuri.alpha7.infra.persistence.AuditableEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuditableEntityTest {

    private static class ConcreteEntity extends AuditableEntity {
        void preCreate() { onCreated(); }
        void preUpdate() { onUpdate(); }
    }

    @Test
    @DisplayName(
            "Given a new entity," +
            " when no fields have been set," +
            " then all timestamp fields are null and isDeleted returns false"
    )
    void shouldHaveNullTimestampsAndNotDeletedInitially() {
        ConcreteEntity entity = new ConcreteEntity();

        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
        assertNull(entity.getDeletedAt());
        assertFalse(entity.isDeleted());
    }

    @Test
    @DisplayName(
            "Given an entity with no deletedAt," +
            " when setDeletedAt is called with a timestamp," +
            " then getDeletedAt returns that timestamp and isDeleted returns true"
    )
    void shouldMarkEntityAsDeletedWhenDeletedAtIsSet() {
        ConcreteEntity entity = new ConcreteEntity();
        LocalDateTime now = LocalDateTime.now();

        entity.setDeletedAt(now);

        assertEquals(now, entity.getDeletedAt());
        assertTrue(entity.isDeleted());
    }

    @Test
    @DisplayName(
            "Given an entity," +
            " when the @PrePersist callback fires," +
            " then createdAt and updatedAt are set to a non-null value"
    )
    void shouldSetTimestampsOnPrePersist() {
        ConcreteEntity entity = new ConcreteEntity();

        entity.preCreate();

        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
    }

    @Test
    @DisplayName(
            "Given an entity with existing timestamps," +
            " when the @PreUpdate callback fires," +
            " then updatedAt is refreshed"
    )
    void shouldRefreshUpdatedAtOnPreUpdate() {
        ConcreteEntity entity = new ConcreteEntity();
        entity.preCreate();
        LocalDateTime original = entity.getUpdatedAt();

        entity.preUpdate();

        assertNotNull(entity.getUpdatedAt());
    }
}
