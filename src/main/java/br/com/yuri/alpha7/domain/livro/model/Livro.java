package br.com.yuri.alpha7.domain.livro.model;

import br.com.yuri.alpha7.domain.autor.model.Autor;
import br.com.yuri.alpha7.domain.editora.model.Editora;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidade de domínio que representa um livro no acervo da biblioteca.
 *
 * <p>A identidade de um livro é determinada pelo seu {@link ISBN}: dois objetos {@code Livro}
 * com o mesmo ISBN são considerados iguais independentemente dos demais campos. Isso permite
 * detectar duplicatas antes de persistir e realizar upsert na importação de CSV.
 *
 * <p>Os campos {@code autores}, {@code editora}, {@code dataPublicacao}, {@code idioma},
 * {@code numeroPaginas} e {@code livrosSemelhantes} são opcionais — a única restrição de
 * domínio é que {@code titulo} e {@code isbn} sejam informados.
 *
 * <p>Esta classe é um POJO puro: não contém anotações JPA nem dependências de framework.
 * A conversão para/de entidade JPA é responsabilidade de {@code LivroMapper}.
 */
public class Livro implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String titulo;
    private ISBN isbn;
    private LocalDate dataPublicacao;
    private Integer numeroPaginas;
    private String idioma;
    private Editora editora;
    private List<Autor> autores = new ArrayList<>();
    private List<Livro> livrosSemelhantes = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public ISBN getIsbn() {
        return isbn;
    }

    public void setIsbn(ISBN isbn) {
        this.isbn = isbn;
    }

    public LocalDate getDataPublicacao() {
        return dataPublicacao;
    }

    public void setDataPublicacao(LocalDate dataPublicacao) {
        this.dataPublicacao = dataPublicacao;
    }

    public Integer getNumeroPaginas() {
        return numeroPaginas;
    }

    public void setNumeroPaginas(Integer numeroPaginas) {
        this.numeroPaginas = numeroPaginas;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public Editora getEditora() {
        return editora;
    }

    public void setEditora(Editora editora) {
        this.editora = editora;
    }

    public List<Autor> getAutores() {
        return autores;
    }

    public void setAutores(List<Autor> autores) {
        this.autores = autores != null ? autores : new ArrayList<>();
    }

    public List<Livro> getLivrosSemelhantes() {
        return livrosSemelhantes;
    }

    public void setLivrosSemelhantes(List<Livro> livrosSemelhantes) {
        this.livrosSemelhantes = livrosSemelhantes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Livro)) return false;
        return Objects.equals(isbn, ((Livro) o).isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }

    @Override
    public String toString() {
        return titulo + " [" + isbn + "]";
    }
}
