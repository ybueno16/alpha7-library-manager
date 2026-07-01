package br.com.yuri.alpha7.infra.persistence.autor;

import br.com.yuri.alpha7.infra.persistence.AuditableEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * Entidade JPA que representa um autor na tabela {@code autor}.
 *
 * <p>Estende {@link AuditableEntity} para herdar os campos de auditoria
 * ({@code created_at}, {@code updated_at}, {@code deleted_at}) e o mecanismo de soft delete.
 *
 * <p>O cache de segundo nível do Hibernate ({@code READ_WRITE}) é habilitado porque autores
 * raramente são alterados após a importação, tornando o cache especialmente eficaz para as
 * consultas de relacionamento realizadas durante o carregamento de livros.
 */
@Entity(name = "Autor")
@Table(name = "autor")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AutorEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "data_falecimento")
    private LocalDate dataFalecimento;

    @Column(columnDefinition = "TEXT")
    private String bio;

    public AutorEntity() {}

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

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public LocalDate getDataFalecimento() {
        return dataFalecimento;
    }

    public void setDataFalecimento(LocalDate dataFalecimento) {
        this.dataFalecimento = dataFalecimento;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
