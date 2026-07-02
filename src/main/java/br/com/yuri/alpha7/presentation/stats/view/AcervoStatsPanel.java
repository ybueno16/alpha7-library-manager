package br.com.yuri.alpha7.presentation.stats.view;

import br.com.yuri.alpha7.application.stats.AcervoStats;
import br.com.yuri.alpha7.application.stats.StatEntry;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * Implementação Swing de {@link AcervoStatsView}: painel de estatísticas do acervo.
 *
 * <p>Organizado em quatro quadrantes:
 * <ol>
 *   <li><b>Resumo</b> — total de livros e distribuição por idioma em tabela.</li>
 *   <li><b>Distribuição por Ano</b> — barras de progresso proporcionais ao ano de maior volume.</li>
 *   <li><b>Top 5 Autores</b> — autores com mais livros cadastrados.</li>
 *   <li><b>Top 5 Editoras</b> — editoras com mais livros cadastrados.</li>
 * </ol>
 *
 * <p>Todos os dados são somente-leitura. O botão "Atualizar" no topo permite recarregar
 * as estatísticas após modificações no acervo.
 */
public class AcervoStatsPanel extends JPanel implements AcervoStatsView {

    private final JButton refreshButton = new JButton("Atualizar");
    private final JLabel  totalLabel    = new JLabel("—");

    private final DefaultTableModel idiomaModel;
    private final DefaultTableModel autoresModel;
    private final DefaultTableModel editorasModel;
    private final JPanel            anoPanel;

    public AcervoStatsPanel() {
        idiomaModel   = tableModel("Idioma",  "Qtd.");
        autoresModel  = tableModel("Autor",   "Qtd.");
        editorasModel = tableModel("Editora", "Qtd.");
        anoPanel      = new JPanel();
        anoPanel.setLayout(new BoxLayout(anoPanel, BoxLayout.Y_AXIS));

        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(buildTopBar(), BorderLayout.NORTH);
        add(buildGrid(),   BorderLayout.CENTER);
    }

    @Override
    public void showStats(AcervoStats stats) {
        totalLabel.setText("Total de livros: " + stats.getTotalLivros());
        populateIdiomas(stats.getLivrosPorIdioma());
        populateRanking(autoresModel,  stats.getTopAutores());
        populateRanking(editorasModel, stats.getTopEditoras());
        populateAnos(stats.getLivrosPorAno());
    }

    @Override
    public void onRefresh(Runnable action) {
        refreshButton.addActionListener(e -> action.run());
    }

    @Override
    public void setLoading(boolean loading) {
        refreshButton.setEnabled(!loading);
        if (loading) {
            totalLabel.setText("Carregando...");
        }
    }

    @Override
    public void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.add(refreshButton);
        return bar;
    }

    private JPanel buildGrid() {
        JTable autoresTable  = readOnlyTable(autoresModel);
        JTable editorasTable = readOnlyTable(editorasModel);

        JPanel grid = new JPanel(new GridLayout(2, 2, 8, 8));
        grid.add(buildResumoPanel());
        grid.add(buildAnoPanel());
        grid.add(buildRankingPanel("Top 5 Autores",  autoresTable));
        grid.add(buildRankingPanel("Top 5 Editoras", editorasTable));
        return grid;
    }

    private JPanel buildResumoPanel() {
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 14f));

        JTable idiomaTable = readOnlyTable(idiomaModel);

        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBorder(BorderFactory.createTitledBorder("Resumo"));
        panel.add(totalLabel,                BorderLayout.NORTH);
        panel.add(new JScrollPane(idiomaTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildAnoPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBorder(BorderFactory.createTitledBorder("Distribuição por Ano"));
        outer.add(new JScrollPane(anoPanel), BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildRankingPanel(String title, JTable table) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void populateIdiomas(Map<String, Long> porIdioma) {
        idiomaModel.setRowCount(0);
        porIdioma.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .forEach(e -> idiomaModel.addRow(new Object[]{e.getKey(), e.getValue()}));
    }

    private void populateRanking(DefaultTableModel model, List<StatEntry> entries) {
        model.setRowCount(0);
        entries.forEach(e -> model.addRow(new Object[]{e.getNome(), e.getTotal()}));
    }

    private void populateAnos(Map<Integer, Long> livrosPorAno) {
        anoPanel.removeAll();

        Map<Integer, Long> ordenado = new TreeMap<>(livrosPorAno);
        long max = ordenado.values().stream().mapToLong(Long::longValue).max().orElse(1L);

        ordenado.forEach((ano, count) -> {
            JLabel label = new JLabel(String.valueOf(ano));
            label.setPreferredSize(new Dimension(44, 20));

            JProgressBar bar = new JProgressBar(0, (int) max);
            bar.setValue(count.intValue());
            bar.setString(count + " livro(s)");
            bar.setStringPainted(true);

            JPanel row = new JPanel(new BorderLayout(6, 0));
            row.add(label, BorderLayout.WEST);
            row.add(bar,   BorderLayout.CENTER);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));

            anoPanel.add(row);
        });

        anoPanel.revalidate();
        anoPanel.repaint();
    }

    private static JTable readOnlyTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setEnabled(false);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        return table;
    }

    private static DefaultTableModel tableModel(String col1, String col2) {
        return new DefaultTableModel(new String[]{col1, col2}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }
}
