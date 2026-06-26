package br.com.yuri.alpha7.infra.persistence.editora;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "Editora")
@Table(name = "editora")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EditoraEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private static final long serialVersionUID = 1L;

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
