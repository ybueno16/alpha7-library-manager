package br.com.yuri.alpha7.presentation.livro.view;

import br.com.yuri.alpha7.domain.autor.model.Autor;
import br.com.yuri.alpha7.domain.livro.model.Livro;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementação Swing de {@link LivroFormView}: janela modal de criação e edição de livros.
 *
 * <p>O diálogo contém campos de texto para todos os atributos do livro (título, ISBN, autores,
 * editora, data de publicação, idioma e número de páginas), um botão "Buscar por ISBN" que
 * delega a busca assíncrona ao presenter, e uma seção de livros semelhantes com lista e botões
 * para adicionar e remover.
 *
 * <p>Este componente não possui lógica de negócio: toda validação e coordenação é responsabilidade
 * do {@link br.com.yuri.alpha7.presentation.livro.presenter.LivroFormPresenter}. A comunicação
 * ocorre exclusivamente através da interface {@link LivroFormView} — o presenter nunca referencia
 * esta classe diretamente, o que permite testar a lógica do formulário com mocks de {@link LivroFormView}.
 */
public class LivroFormDialog extends JDialog implements LivroFormView {

    private final JTextField titleField   = new JTextField(30);
    private final JTextField isbnField    = new JTextField(20);
    private final JTextField autoresField = new JTextField(30);
    private final JTextField editoraField = new JTextField(20);
    private final JTextField dateField    = new JTextField(10);
    private final JTextField idiomaField  = new JTextField(10);
    private final JTextField pagesField   = new JTextField(6);
    private final JButton lookupButton    = new JButton("Buscar por ISBN");
    private final JButton saveButton      = new JButton("Salvar");
    private final JButton cancelButton    = new JButton("Cancelar");
    private final JLabel  validationLabel = new JLabel(" ");

    private final DefaultListModel<Livro> semelhantesModel  = new DefaultListModel<>();
    private final JList<Livro>            semelhantesList   = new JList<>(semelhantesModel);
    private final JButton                 addSemelanteBtn   = new JButton("Adicionar");
    private final JButton                 removeSemelanteBtn = new JButton("Remover");

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

        addRow(form, lbl, fld, 0, "Título *:",   titleField);
        addRow(form, lbl, fld, 1, "ISBN *:",     isbnPanel);
        addRow(form, lbl, fld, 2, "Autores *:",  autoresField);
        addRow(form, lbl, fld, 3, "Editora:",    editoraField);
        addRow(form, lbl, fld, 4, "Publicação:", dateField);
        addRow(form, lbl, fld, 5, "Idioma:",     idiomaField);
        addRow(form, lbl, fld, 6, "Páginas:",    pagesField);

        JPanel semelhantesPanel = buildSemelhantesPanel();

        validationLabel.setForeground(Color.RED);
        validationLabel.setBorder(BorderFactory.createEmptyBorder(0, 12, 4, 12));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(saveButton);
        buttons.add(cancelButton);

        JPanel south = new JPanel(new BorderLayout());
        south.add(validationLabel, BorderLayout.NORTH);
        south.add(buttons,         BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(form,              BorderLayout.NORTH);
        add(semelhantesPanel,  BorderLayout.CENTER);
        add(south,             BorderLayout.SOUTH);
    }

    private JPanel buildSemelhantesPanel() {
        semelhantesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(semelhantesList);
        scroll.setPreferredSize(new Dimension(0, 100));

        JPanel semelanteButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        semelanteButtons.add(addSemelanteBtn);
        semelanteButtons.add(removeSemelanteBtn);

        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(4, 12, 8, 12), "Livros Semelhantes"));
        panel.add(scroll,           BorderLayout.CENTER);
        panel.add(semelanteButtons, BorderLayout.SOUTH);
        return panel;
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
        c.gridx  = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(4, 0, 4, 8);
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
        dateField.setText(livro.getDataPublicacao() != null
                ? String.valueOf(livro.getDataPublicacao().getYear()) : "");
        idiomaField.setText(livro.getIdioma() != null ? livro.getIdioma() : "");
        pagesField.setText(livro.getNumeroPaginas() != null
                ? livro.getNumeroPaginas().toString() : "");
    }

    @Override
    public List<Livro> getLivrosSemelhantes() {
        List<Livro> list = new ArrayList<>();
        for (int i = 0; i < semelhantesModel.size(); i++) {
            list.add(semelhantesModel.getElementAt(i));
        }
        return list;
    }

    @Override
    public void setLivrosSemelhantes(List<Livro> semelhantes) {
        semelhantesModel.clear();
        semelhantes.forEach(semelhantesModel::addElement);
    }

    @Override
    public Optional<Livro> getSelectedSemelhante() {
        return Optional.ofNullable(semelhantesList.getSelectedValue());
    }

    @Override
    public Optional<Livro> pickSemelhante(List<Livro> disponiveis) {
        if (disponiveis.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Não há outros livros disponíveis para adicionar como semelhantes.",
                    "Sem livros disponíveis", JOptionPane.INFORMATION_MESSAGE);
            return Optional.empty();
        }
        Livro[] opcoes = disponiveis.toArray(new Livro[0]);
        Livro escolhido = (Livro) JOptionPane.showInputDialog(
                this,
                "Selecione um livro semelhante:",
                "Adicionar livro semelhante",
                JOptionPane.PLAIN_MESSAGE,
                null,
                opcoes,
                opcoes[0]);
        return Optional.ofNullable(escolhido);
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
    public void onAddSemelhante(Runnable acao) {
        addSemelanteBtn.addActionListener(e -> acao.run());
    }

    @Override
    public void onRemoveSemelhante(Runnable acao) {
        removeSemelanteBtn.addActionListener(e -> acao.run());
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
    public void setLookupEnabled(boolean enabled) {
        lookupButton.setEnabled(enabled);
        lookupButton.setText(enabled ? "Buscar por ISBN" : "Buscando");
    }

    @Override
    public void showValidationError(String message) {
        validationLabel.setText(message);
    }

    @Override
    public void clearValidationError() {
        validationLabel.setText(" ");
    }
}
