package br.com.yuri.alpha7.application.importacao.model;

/**
 * DTO que representa um registro de livro lido de um arquivo de importação,
 * independente do formato de origem (CSV, XML, etc.).
 */
public class ImportRecord {

    private final String titulo;
    private final String isbn;
    private final String autores;
    private final String editora;
    private final String dataPublicacao;
    private final String idioma;
    private final String numeroPaginas;

    public ImportRecord(
            String titulo,
            String isbn,
            String autores,
            String editora,
            String dataPublicacao,
            String idioma,
            String numeroPaginas
    ) {
        this.titulo         = titulo;
        this.isbn           = isbn;
        this.autores        = autores;
        this.editora        = editora;
        this.dataPublicacao = dataPublicacao;
        this.idioma         = idioma;
        this.numeroPaginas  = numeroPaginas;
    }

    public String getTitulo()         { return titulo; }
    public String getIsbn()           { return isbn; }
    public String getAutores()        { return autores; }
    public String getEditora()        { return editora; }
    public String getDataPublicacao() { return dataPublicacao; }
    public String getIdioma()         { return idioma; }
    public String getNumeroPaginas()  { return numeroPaginas; }
}
