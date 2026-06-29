package br.com.yuri.alpha7.presentation.livro.view;

import br.com.yuri.alpha7.application.importacao.ImportPreviewRecord;
import br.com.yuri.alpha7.application.importacao.ImportPreviewRecord.Status;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.List;

/**
 * Dialog de preview da importação.
 *
 * <p>Exibe cada linha do arquivo com seu status antes de gravar qualquer dado.
 * O usuário pode marcar/desmarcar individualmente quais linhas importar.
 * Linhas com ERRO têm o checkbox desabilitado.
 */
public class ImportPreviewDialog extends JDialog {

    private static final Color COLOR_NOVO      = new Color(198, 239, 206);
    private static final Color COLOR_JA_EXISTE = new Color(255, 235, 156);
    private static final Color COLOR_ERRO      = new Color(255, 199, 206);

    private final PreviewTableModel tableModel;
    private boolean confirmed = false;

    public ImportPreviewDialog(Frame parent, List<ImportPreviewRecord> records) {
        super(parent, "Preview da importação", true);
        tableModel = new PreviewTableModel(records);
        initLayout(records);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(parent);
    }

    private void initLayout(List<ImportPreviewRecord> records) {
        long novos     = records.stream().filter(r -> r.getStatus() == Status.NOVO).count();
        long jaExistem = records.stream().filter(r -> r.getStatus() == Status.JA_EXISTE).count();
        long erros     = records.stream().filter(r -> r.getStatus() == Status.ERRO).count();

        JLabel summary = new JLabel(String.format(
                "<html><b>%d</b> para importar &nbsp;|&nbsp; <b>%d</b> já existem &nbsp;|&nbsp; <b>%d</b> com erro</html>",
                novos, jaExistem, erros));
        summary.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JTable table = new JTable(tableModel);
        table.setRowHeight(24);
        table.getColumnModel().getColumn(0).setPreferredWidth(30);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setMaxWidth(60);
        table.getColumnModel().getColumn(2).setPreferredWidth(260);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(130);

        StatusCellRenderer renderer = new StatusCellRenderer();
        for (int col = 1; col <= 4; col++) {
            table.getColumnModel().getColumn(col).setCellRenderer(renderer);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(660, 320));
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        JButton importButton = new JButton("Importar selecionados");
        JButton cancelButton = new JButton("Cancelar");

        importButton.addActionListener(e -> {
            confirmed = true;
            dispose();
        });
        cancelButton.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttons.add(importButton);
        buttons.add(cancelButton);

        setLayout(new BorderLayout());
        add(summary, BorderLayout.NORTH);
        add(scroll,  BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    private static class PreviewTableModel extends AbstractTableModel {

        private static final String[] COLUMNS = {"", "Linha", "Título", "ISBN", "Status"};

        private final List<ImportPreviewRecord> records;

        PreviewTableModel(List<ImportPreviewRecord> records) {
            this.records = records;
        }

        @Override
        public int getRowCount() {
            return records.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int col) {
            return COLUMNS[col];
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return col == 0 ? Boolean.class : String.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            if (col != 0) return false;
            return records.get(row).getStatus() != Status.ERRO;
        }

        @Override
        public Object getValueAt(int row, int col) {
            ImportPreviewRecord r = records.get(row);
            switch (col) {
                case 0: return r.isSelecionado();
                case 1: return String.valueOf(r.getLineNumber());
                case 2: return r.getTitulo();
                case 3: return r.getIsbn();
                case 4: return r.getMensagem();
                default: return "";
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col != 0) return;
            records.get(row).setSelecionado((Boolean) value);
            fireTableCellUpdated(row, col);
        }

        ImportPreviewRecord getRecord(int row) {
            return records.get(row);
        }
    }

    private class StatusCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            if (!isSelected) applyBackground(c, tableModel.getRecord(row).getStatus());
            setHorizontalAlignment(col == 1 ? SwingConstants.CENTER : SwingConstants.LEFT);
            return c;
        }

        private void applyBackground(Component c, Status status) {
            switch (status) {
                case NOVO:      c.setBackground(COLOR_NOVO);      break;
                case JA_EXISTE: c.setBackground(COLOR_JA_EXISTE); break;
                case ERRO:      c.setBackground(COLOR_ERRO);       break;
            }
        }
    }
}
