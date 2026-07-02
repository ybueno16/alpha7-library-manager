package br.com.yuri.alpha7.presentation.livro.view;

import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.presentation.livro.presenter.LivroTableModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Implementação Swing de {@link LivroListView}: painel principal do sistema com a tabela de livros.
 *
 * <p>Contém uma barra de busca com campo de texto e botão "Buscar", a tabela de livros com ordenação
 * por coluna (via {@link TableRowSorter}), e botões de ação: Novo, Editar, Excluir, Importar e Exportar.
 * Os botões Editar e Excluir ficam desabilitados enquanto nenhum livro está selecionado.
 *
 * <p>Atalhos de teclado registrados:
 * <ul>
 *   <li>{@code F5} — executa a busca</li>
 *   <li>{@code Ctrl+N} — abre o formulário de novo livro</li>
 *   <li>{@code Delete} (com foco na tabela) — exclui os livros selecionados</li>
 *   <li>Duplo clique em uma linha — abre o formulário de edição</li>
 * </ul>
 *
 * <p>Assim como {@link LivroFormDialog}, este componente é Passive View: nenhuma lógica de negócio
 * ou acesso a dados reside aqui. Toda coordenação é feita pelo
 * {@link br.com.yuri.alpha7.presentation.livro.presenter.LivroListPresenter}.
 */
public class LivroListPanel extends JPanel implements LivroListView {

    private final LivroTableModel   tableModel      = new LivroTableModel();
    private final JTable            table           = new JTable(tableModel);
    private final JTextField        searchField     = new JTextField(20);
    private final JButton           searchButton    = new JButton("Buscar");
    private final JButton           newButton       = new JButton("Novo");
    private final JButton           editButton      = new JButton("Editar");
    private final JButton           deleteButton    = new JButton("Excluir");
    private final JButton           importButton    = new JButton("Importar");
    private final JButton           exportButton    = new JButton("Exportar");
    private final JButton           prevButton      = new JButton("< Anterior");
    private final JButton           nextButton      = new JButton("Próximo >");
    private final JLabel            pageLabel       = new JLabel("Página 1 de 1");
    private final JTextField        autorFiltroField = new JTextField(14);
    private final JComboBox<String> editoraCombo    = new JComboBox<>();
    private final JTextField        anoDeField      = new JTextField(5);
    private final JTextField        anoAteField     = new JTextField(5);
    private final JComboBox<String> idiomaCombo     = new JComboBox<>();

    private Runnable searchAction;
    private Runnable createAction;
    private Runnable editAction;
    private Runnable deleteAction;

    public LivroListPanel() {
        initComponents();
        initLayout();
        initKeyBindings();
    }

    private void initComponents() {
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        TableRowSorter<LivroTableModel> sorter = new TableRowSorter<>(tableModel);
        Comparator<Integer> nullSafe = Comparator.nullsFirst(Comparator.naturalOrder());
        sorter.setComparator(4, nullSafe);
        sorter.setComparator(6, nullSafe);
        table.setRowSorter(sorter);
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        prevButton.setEnabled(false);
        nextButton.setEnabled(false);

        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRender);
        table.getColumnModel().getColumn(6).setCellRenderer(centerRender);

        table.getSelectionModel().addListSelectionListener(e -> {
            int count = table.getSelectedRowCount();
            editButton.setEnabled(count == 1);
            deleteButton.setEnabled(count >= 1);
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && editAction != null) {
                    editAction.run();
                }
            }
        });
    }

    private void initLayout() {
        setLayout(new BorderLayout(0, 4));

        editoraCombo.addItem("");
        idiomaCombo.addItem("");

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));

        filterPanel.add(new JLabel("Buscar:"));
        filterPanel.add(searchField);
        filterPanel.add(searchButton);

        filterPanel.add(new JLabel("Autor:"));
        filterPanel.add(autorFiltroField);

        filterPanel.add(new JLabel("Editora:"));
        filterPanel.add(editoraCombo);

        filterPanel.add(new JLabel("De:"));
        filterPanel.add(anoDeField);

        filterPanel.add(new JLabel("Até:"));
        filterPanel.add(anoAteField);

        filterPanel.add(new JLabel("Idioma:"));
        filterPanel.add(idiomaCombo);

        add(filterPanel, BorderLayout.NORTH);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        actionPanel.add(newButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);
        actionPanel.add(importButton);
        actionPanel.add(exportButton);

        JPanel pagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        pagePanel.add(prevButton);
        pagePanel.add(pageLabel);
        pagePanel.add(nextButton);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(pagePanel, BorderLayout.NORTH);
        southPanel.add(actionPanel, BorderLayout.SOUTH);

        add(southPanel, BorderLayout.SOUTH);
    }

    private void initKeyBindings() {
        bindKey("search", KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),
                () -> { if (searchAction != null) searchAction.run(); });
        bindKey("new", KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK),
                () -> { if (createAction != null) createAction.run(); });

        table.getInputMap(JComponent.WHEN_FOCUSED)
             .put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete-row");
        table.getActionMap()
             .put("delete-row", new AbstractAction() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if (deleteAction != null) deleteAction.run();
                 }
             });
    }

    private void bindKey(String name, KeyStroke ks, Runnable action) {
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ks, name);
        getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
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
        if (row < 0) {
            return Optional.empty();
        }
        int modelRow = table.convertRowIndexToModel(row);
        return Optional.of(tableModel.getLivro(modelRow));
    }

    @Override
    public List<Livro> getSelectedLivros() {
        int[] rows = table.getSelectedRows();
        List<Livro> result = new ArrayList<>();
        for (int row : rows) {
            int modelRow = table.convertRowIndexToModel(row);
            result.add(tableModel.getLivro(modelRow));
        }
        return result;
    }

    @Override
    public void showPaginationInfo(int currentPage, int totalPages) {
        pageLabel.setText("Página " + currentPage + " de " + totalPages);
        prevButton.setEnabled(currentPage > 1);
        nextButton.setEnabled(currentPage < totalPages);
    }

    @Override
    public void onNextPage(Runnable acao) {
        nextButton.addActionListener(e -> acao.run());
    }

    @Override
    public void onPreviousPage(Runnable acao) {
        prevButton.addActionListener(e -> acao.run());
    }

    @Override
    public void onSearch(Runnable acao) {
        this.searchAction = acao;
        searchButton.addActionListener(e -> acao.run());
        searchField.addActionListener(e -> acao.run());
    }

    @Override
    public void onCreate(Runnable acao) {
        this.createAction = acao;
        newButton.addActionListener(e -> acao.run());
    }

    @Override
    public void onEdit(Runnable acao) {
        this.editAction = acao;
        editButton.addActionListener(e -> acao.run());
    }

    @Override
    public void onDelete(Runnable acao) {
        this.deleteAction = acao;
        deleteButton.addActionListener(e -> acao.run());
    }

    @Override
    public void onImport(Runnable acao) {
        importButton.addActionListener(e -> acao.run());
    }

    @Override
    public void onExport(Runnable acao) {
        exportButton.addActionListener(e -> acao.run());
    }

    @Override
    public boolean confirm(String message) {
        int result = JOptionPane.showConfirmDialog(this, message, "Confirmar", JOptionPane.YES_NO_OPTION);
        return result == JOptionPane.YES_OPTION;
    }

    @Override
    public void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public String getAutorFiltro() {
        return autorFiltroField.getText();
    }

    @Override
    public String getEditoraFiltro() {
        Object sel = editoraCombo.getSelectedItem();
        return sel != null ? sel.toString() : "";
    }

    @Override
    public String getAnoDe() {
        return anoDeField.getText();
    }

    @Override
    public String getAnoAte() {
        return anoAteField.getText();
    }

    @Override
    public String getIdiomaFiltro() {
        Object sel = idiomaCombo.getSelectedItem();
        return sel != null ? sel.toString() : "";
    }

    @Override
    public void setEditoraOptions(List<String> editoras) {
        String current = getEditoraFiltro();
        editoraCombo.removeAllItems();
        editoraCombo.addItem("");
        for (String e : editoras) {
            editoraCombo.addItem(e);
        }
        editoraCombo.setSelectedItem(current);
    }

    @Override
    public void setIdiomaOptions(List<String> idiomas) {
        String current = getIdiomaFiltro();
        idiomaCombo.removeAllItems();
        idiomaCombo.addItem("");
        for (String i : idiomas) {
            idiomaCombo.addItem(i);
        }
        idiomaCombo.setSelectedItem(current);
    }
}
