package br.com.yuri.alpha7.application.importacao.model;

/**
 * Representa o resultado da validação de uma linha do arquivo de importação antes de gravar.
 *
 * <p>Cada instância carrega o status da linha ({@link Status}), os dados visíveis ao usuário
 * e o {@link ImportRecord} original, necessário para gravar caso o usuário confirme a importação.
 */
public class ImportPreviewRecord {

    public enum Status {
        NOVO,
        JA_EXISTE,
        ERRO
    }

    private final int lineNumber;
    private final String titulo;
    private final String isbn;
    private final Status status;
    private final String mensagem;
    private boolean selecionado;
    private final ImportRecord sourceRecord;

    public ImportPreviewRecord(int lineNumber,
                               String titulo,
                               String isbn,
                               Status status,
                               String mensagem,
                               boolean selecionado,
                               ImportRecord sourceRecord) {
        this.lineNumber   = lineNumber;
        this.titulo       = titulo;
        this.isbn         = isbn;
        this.status       = status;
        this.mensagem     = mensagem;
        this.selecionado  = selecionado;
        this.sourceRecord = sourceRecord;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getIsbn() {
        return isbn;
    }

    public Status getStatus() {
        return status;
    }

    public String getMensagem() {
        return mensagem;
    }

    public boolean isSelecionado() {
        return selecionado;
    }

    public void setSelecionado(boolean selecionado) {
        this.selecionado = selecionado;
    }

    public ImportRecord getSourceRecord() {
        return sourceRecord;
    }

}
