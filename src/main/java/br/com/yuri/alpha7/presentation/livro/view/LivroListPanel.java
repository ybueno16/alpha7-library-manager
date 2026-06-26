package br.com.yuri.alpha7.presentation.livro.view;

import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.presentation.livro.presenter.LivroTableModel;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Optional;

public class LivroListPanel extends JPanel implements LivroListView {

    private final LivroTableModel tableModel      = new LivroTableModel();
    private final JTable          table           = new JTable(tableModel);
    private final JTextField      searchField     = new JTextField(20);
    private final JButton         searchButton    = new JButton("Buscar");
    private final JButton         newButton       = new JButton("Novo");
    private final JButton         editButton      = new JButton("Editar");
    private final JButton         deleteButton    = new JButton("Excluir");
    private final JButton         importButton    = new JButton("Importar CSV");


    public LivroListPanel() {
        initComponents();
        initLayout();
    }

    private void initComponents (){
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        table.getSelectionModel().addListSelectionListener(e -> {
            boolean selected = table.getSelectedRow() >= 0;
            editButton.setEnabled(selected);
            deleteButton.setEnabled(selected);
        });
    }

    private void initLayout(){
        setLayout(new BorderLayout(0, 8));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Buscar:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        add(searchPanel, BorderLayout.NORTH);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(newButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(importButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    @Override
    public void showLivros(List<Livro> livros) {
        tableModel.setLivros(livros);
    }

    @Override
    public String getSearchTerm() {
        return searchField.getText();
    }

    @Override
    public Optional<Livro> getSelectedLivro() {
        int row = table.getSelectedRow();
        if(row < 0) return Optional.empty();
        int modelRow = table.convertRowIndexToModel(row);
        return Optional.of(tableModel.getLivro(modelRow));
    }

    @Override
    public void onImport(Runnable acao) {
        importButton.addActionListener(e -> acao.run());
    }

    @Override
    public void onSearch(Runnable acao) {
        searchButton.addActionListener(e -> acao.run());
        searchField.addActionListener(e -> acao.run());
    }

    @Override
    public void onCreate(Runnable acao) {
        newButton.addActionListener(e -> acao.run());
    }

    @Override
    public void onEdit(Runnable acao) {
        editButton.addActionListener(e -> acao.run());
    }

    @Override
    public void onDelete(Runnable acao) {
        deleteButton.addActionListener(e -> acao.run());
    }

    @Override
    public void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
