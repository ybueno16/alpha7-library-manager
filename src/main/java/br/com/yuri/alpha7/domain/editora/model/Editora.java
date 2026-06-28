package br.com.yuri.alpha7.domain.editora.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entidade de domínio que representa a editora responsável pela publicação de um livro.
 *
 * <p>A identidade de uma editora é determinada pelo {@code nome}: dois objetos {@code Editora}
 * com o mesmo nome são considerados iguais. Na importação de CSV e na busca por ISBN, o sistema
 * pesquisa primeiro uma editora existente pelo nome antes de criar uma nova, evitando duplicatas.
 *
 * <p>Esta classe é um POJO puro, sem anotações JPA. A conversão para/de entidade JPA é
 * responsabilidade de {@code EditoraMapper}.
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
