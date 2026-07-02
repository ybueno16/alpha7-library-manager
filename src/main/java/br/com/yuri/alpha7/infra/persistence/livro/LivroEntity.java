package br.com.yuri.alpha7.infra.persistence.livro;

import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import br.com.yuri.alpha7.infra.persistence.AuditableEntity;
import br.com.yuri.alpha7.infra.persistence.autor.AutorEntity;
import br.com.yuri.alpha7.infra.persistence.editora.EditoraEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade JPA principal que representa um livro na tabela {@code livro}.
 *
 * <p>Relacionamentos:
 * <ul>
 *   <li>{@code @ManyToMany} com {@link AutorEntity} via tabela de junção {@code livro_autor}.
 *       O cascade {@code PERSIST/MERGE} permite que autores novos sejam gravados junto com o livro.</li>
 *   <li>{@code @ManyToMany} com {@link LivroEntity} (auto-relacionamento) via {@code livro_semelhante}
 *       para a funcionalidade de livros semelhantes. Sem cascade — semelhantes já existem no banco.</li>
 *   <li>{@code @ManyToOne} com {@link EditoraEntity}: editora pode ser nula.</li>
 * </ul>
 *
 * <p>O ISBN é persistido pelo {@link br.com.yuri.alpha7.infra.persistence.converter.IsbnConverter},
 * que converte o Value Object {@link br.com.yuri.alpha7.domain.livro.vo.ISBN} para {@code String}
 * e vice-versa. A unicidade do ISBN é garantida no banco por um índice único parcial
 * ({@code uq_livro_isbn_ativo}, criado na migration {@code V7}) que só considera livros ativos —
 * não pela anotação JPA, já que o schema é gerido por Flyway e não por geração automática do
 * Hibernate. Essa constraint é a segunda linha de defesa (a primeira é a validação em
 * {@link br.com.yuri.alpha7.application.livro.BookCrudUseCase}).
 */
@Entity(name = "Livro")
@Table(name = "livro")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class LivroEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 512)
    private String titulo;

    @Column(nullable = false, length = 20)
    private ISBN isbn;

    @Column(name = "data_publicacao")
    private LocalDate dataPublicacao;

    @Column(name = "numero_paginas")
    private Integer numeroPaginas;

    @Column(length = 50)
    private String idioma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "editora_id")
    private EditoraEntity editora;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "livro_autor",
        joinColumns = @JoinColumn(name = "livro_id"),
        inverseJoinColumns = @JoinColumn(name = "autor_id")
    )
    private List<AutorEntity> autores = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "livro_semelhante",
        joinColumns = @JoinColumn(name = "livro_id"),
        inverseJoinColumns = @JoinColumn(name = "semelhante_id")
    )
    private List<LivroEntity> livrosSemelhantes = new ArrayList<>();

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

    public EditoraEntity getEditora() {
        return editora;
    }

    public void setEditora(EditoraEntity editora) {
        this.editora = editora;
    }

    public List<AutorEntity> getAutores() {
        return autores;
    }

    public void setAutores(List<AutorEntity> autores) {
        this.autores = autores;
    }

    public List<LivroEntity> getLivrosSemelhantes() {
        return livrosSemelhantes;
    }

    public void setLivrosSemelhantes(List<LivroEntity> livrosSemelhantes) {
        this.livrosSemelhantes = livrosSemelhantes;
    }
}
