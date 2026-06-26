package br.com.yuri.alpha7.domain.editora.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Representa a editora responsável pela publicação de um livro.
 */
public class Editora implements Serializable {

    private Long id;
    private String nome;

    private static final long serialVersionUID = 1L;

    public Editora() {}

    public Editora(String nome) {
        this.nome = nome;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Editora)) return false;
        return Objects.equals(nome, ((Editora) o).nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome);
    }

    @Override
    public String toString() {
        return nome;
    }
}
