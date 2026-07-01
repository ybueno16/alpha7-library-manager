package br.com.yuri.alpha7.infra.persistence;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Superclasse mapeada que fornece campos de auditoria para todas as entidades JPA.
 *
 * <p>{@code createdAt} é preenchido uma única vez no momento da primeira persistência
 * e nunca mais alterado ({@code updatable = false}). {@code updatedAt} é atualizado
 * em cada operação de escrita subsequente.
 *
 * <p>Os callbacks {@link PrePersist} e {@link PreUpdate} são disparados pelo Hibernate
 * antes de executar o INSERT e o UPDATE respectivamente, garantindo que os valores
 * sejam sempre definidos pela aplicação sem depender de defaults do banco.
 */
@MappedSuperclass
public abstract class AuditableEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreated() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
