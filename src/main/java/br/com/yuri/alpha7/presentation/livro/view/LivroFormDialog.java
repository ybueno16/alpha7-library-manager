package br.com.yuri.alpha7.presentation.livro.view;

import br.com.yuri.alpha7.domain.autor.model.Autor;
import br.com.yuri.alpha7.domain.livro.model.Livro;

import javax.swing.*;
import java.awt.*;
import java.util.stream.Collectors;

public class LivroFormDialog extends JDialog implements LivroFormView {

    private final JTextField titleField   = new JTextField(30);
    private final JTextField isbnField    = new JTextField(20);
    private final JTextField autoresField = new JTextField(30);
    private final JTextField editoraField = new JTextField(20);
    private final JTextField dateField    = new JTextField(10);
    private final JTextField idiomaField  = new JTextField(10);
    private final JTextField pagesField   = new JTextField(6);
    private final JButton    lookupButton = new JButton("Buscar por ISBN");
    private final JButton    saveButton   = new JButton("Salvar");
    private final JButton    cancelButton = new JButton("Cancelar");

    public LivroFormDialog(Frame parent) {
        super(parent, "Livro", true);
        initLayout();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(parent);
    }

    private void initLayout() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));
        GridBagConstraints lbl = labelConstraints();
        GridBagConstraints fld = fieldConstraints();

        JPanel isbnPanel = new JPanel(new BorderLayout(4, 0));
        isbnPanel.add(isbnField,    BorderLayout.CENTER);
        isbnPanel.add(lookupButton, BorderLayout.EAST);

        addRow(form, lbl, fld, 0, "Título *:",    titleField);
        addRow(form, lbl, fld, 1, "ISBN *:",      isbnPanel);
        addRow(form, lbl, fld, 2, "Autores *:",   autoresField);
        addRow(form, lbl, fld, 3, "Editora:",     editoraField);
        addRow(form, lbl, fld, 4, "Publicação:",  dateField);
        addRow(form, lbl, fld, 5, "Idioma:",      idiomaField);
        addRow(form, lbl, fld, 6, "Páginas:",     pagesField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(saveButton);
        buttons.add(cancelButton);

        setLayout(new BorderLayout());
        add(form,    BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private void addRow(JPanel panel, GridBagConstraints lbl, GridBagConstraints fld,
                        int row, String label, JComponent field) {
        lbl.gridy = row;
        fld.gridy = row;
        panel.add(new JLabel(label), lbl);
        panel.add(field,             fld);
    }

    private GridBagConstraints labelConstraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx   = 0;
        c.anchor  = GridBagConstraints.WEST;
        c.insets  = new Insets(4, 0, 4, 8);
        return c;
    }

    private GridBagConstraints fieldConstraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx   = 1;
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets  = new Insets(4, 0, 4, 0);
        return c;
    }

    @Override
    public String getTitulo() {
        return titleField.getText().trim();
    }

    @Override
    public String getIsbn() {
        return isbnField.getText().trim();
    }

    @Override
    public String getAutores() {
        return autoresField.getText().trim();
    }

    @Override
    public String getEditora() {
        return editoraField.getText().trim();
    }

    @Override
    public String getDataPublicacao() {
        return dateField.getText().trim();
    }

    @Override
    public String getIdioma() {
        return idiomaField.getText().trim();
    }

    @Override
    public String getNumeroPaginas() {
        return pagesField.getText().trim();
    }

    @Override
    public void setLivro(Livro livro) {
        titleField.setText(livro.getTitulo());
        isbnField.setText(livro.getIsbn().getValue());
        autoresField.setText(livro.getAutores().stream()
                .map(Autor::getNome).collect(Collectors.joining(", ")));
        editoraField.setText(livro.getEditora() != null ? livro.getEditora().getNome() : "");
        dateField.setText(livro.getDataPublicacao() != null ? String.valueOf(livro.getDataPublicacao().getYear()) : "");
        idiomaField.setText(livro.getIdioma() != null ? livro.getIdioma() : "");
        pagesField.setText(livro.getNumeroPaginas() != null ? livro.getNumeroPaginas().toString() : "");
    }

    @Override
    public void onIsbnLookup(Runnable acao) {
        lookupButton.addActionListener(e -> acao.run());
    }

    @Override
    public void onSave(Runnable acao) {
        saveButton.addActionListener(e -> acao.run());
    }
    @Override
    public void onCancel(Runnable acao) {
        cancelButton.addActionListener(e -> acao.run());
    }

    @Override
    public void close() {
        dispose();
    }

    @Override
    public void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void setLookupEnabled(boolean enabled){
        lookupButton.setEnabled(enabled);
        lookupButton.setText(enabled ? "Buscar por ISBN" : "Buscando");
    }
}
