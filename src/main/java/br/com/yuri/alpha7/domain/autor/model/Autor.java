package br.com.yuri.alpha7.domain.autor.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidade de domínio que representa um autor de livros no acervo da biblioteca.
 *
 * <p>A identidade de um autor é determinada pelo {@code nome}: dois objetos {@code Autor}
 * com o mesmo nome são considerados iguais. Isso é usado na importação de CSV e na busca
 * por ISBN via OpenLibrary para evitar duplicar autores já cadastrados.
 *
 * <p>Os campos {@code dataNascimento}, {@code dataFalecimento} e {@code bio} são opcionais
 * e preenchidos automaticamente quando o livro é obtido via API da OpenLibrary.
 *
 * <p>Esta classe é um POJO puro, sem anotações JPA. A conversão para/de entidade JPA é
 * responsabilidade de {@code AutorMapper}.
 */
public class Autor implements Serializable {

    private Long id;
    private String nome;
    private LocalDate dataNascimento;
    private LocalDate dataFalecimento;
    private String bio;

    private static final long serialVersionUID = 1L;

    public Autor() {}

    public Autor(String nome) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Autor)) return false;
        return Objects.equals(nome, ((Autor) o).nome);
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
