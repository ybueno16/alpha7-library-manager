package br.com.yuri.alpha7.presentation.livro.presenter;

import br.com.yuri.alpha7.domain.autor.model.Autor;
import br.com.yuri.alpha7.domain.livro.model.Livro;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LivroTableModel extends AbstractTableModel {

    private static final String[] COLUNAS = {
            "Título", "ISBN", "Autores", "Editora", "Publicação", "Idioma", "Páginas"
    };

    private List<Livro> livros = new ArrayList<>();

    @Override
    public int getRowCount() {
        return livros.size();
    }

    @Override
    public int getColumnCount() {
        return COLUNAS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUNAS[column];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Livro livro = livros.get(rowIndex);
        switch (columnIndex) {
            case 0: return livro.getTitulo();
            case 1: return livro.getIsbn().getValue();
            case 2: return formatAuthors(livro);
            case 3: return livro.getEditora() != null ? livro.getEditora().getNome() : "";
            case 4: return livro.getDataPublicacao() != null ? livro.getDataPublicacao().getYear() : "";
            case 5: return livro.getIdioma() != null ? livro.getIdioma() : "";
            case 6: return livro.getNumeroPaginas() != null ? livro.getNumeroPaginas() : "";
            default: return "";
        }
    }

    public Livro getLivro(int row) {
        return livros.get(row);
    }

    public void setLivros(List<Livro> livros) {
        this.livros = livros;
        fireTableDataChanged();
    }

    private String formatAuthors(Livro livro) {
        return livro.getAutores().stream()
                .map(Autor::getNome)
                .collect(Collectors.joining(", "));
    }
}
