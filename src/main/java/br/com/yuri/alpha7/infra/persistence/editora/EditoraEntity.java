package br.com.yuri.alpha7.infra.persistence.editora;

import br.com.yuri.alpha7.infra.persistence.AuditableEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

/**
 * Entidade JPA que representa uma editora na tabela {@code editora}.
 *
 * <p>Editoras são compartilhadas entre livros: quando um livro é salvo com um nome de editora
 * já cadastrado, a referência existente é reaproveitada pelo {@link br.com.yuri.alpha7.application.editora.EditoraUseCase}.
 * O cache de segundo nível ({@code READ_WRITE}) reduz as consultas de lookup repetitivo durante
 * importações em lote.
 */
@Entity(name = "Editora")
@Table(name = "editora")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EditoraEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    public EditoraEntity() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
