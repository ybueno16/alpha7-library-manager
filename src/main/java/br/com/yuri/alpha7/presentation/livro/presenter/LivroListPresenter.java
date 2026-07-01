package br.com.yuri.alpha7.presentation.livro.presenter;

import br.com.yuri.alpha7.application.importacao.model.ImportPreviewRecord;
import br.com.yuri.alpha7.application.importacao.model.ImportResult;
import br.com.yuri.alpha7.application.importacao.ImportUseCase;
import br.com.yuri.alpha7.application.isbn.IsbnLookupUseCase;
import br.com.yuri.alpha7.application.livro.BookCrudUseCase;
import br.com.yuri.alpha7.application.livro.BookExportUseCase;
import br.com.yuri.alpha7.application.livro.BookSearchUseCase;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.presentation.livro.view.ImportPreviewDialog;
import br.com.yuri.alpha7.presentation.livro.view.LivroFormDialog;
import br.com.yuri.alpha7.presentation.livro.view.LivroListView;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

/**
 * Presenter responsável por toda a lógica da tela de listagem de livros.
 *
 * <p>Ao ser instanciado, registra callbacks na view para busca, exclusão, criação, edição e
 * importação. O estado da tela (lista exibida, livro selecionado) fica na view; o presenter
 * apenas reage a eventos e direciona o fluxo.
 *
 * <p>A operação de exclusão exige confirmação explícita do usuário via
 * {@link br.com.yuri.alpha7.presentation.livro.view.LivroListView#confirm(String)}.
 * Ao confirmar, o presenter delega ao {@link br.com.yuri.alpha7.application.livro.BookCrudUseCase}
 * e em seguida recarrega a lista.
 *
 * <p>As operações de criação e edição abrem um {@link br.com.yuri.alpha7.presentation.livro.view.LivroFormDialog}
 * e configuram um {@link LivroFormPresenter} que, ao salvar com sucesso, chama
 * {@link #loadLivros()} para atualizar a tabela.
 */
public class LivroListPresenter {

    private final Frame            parent;
    private final LivroListView     view;
    private final BookSearchUseCase searchUseCase;
    private final BookCrudUseCase   crudUseCase;
    private final BookExportUseCase exportUseCase;
    private final ImportUseCase     importUseCase;
    private final IsbnLookupUseCase isbnLookupUseCase;

    public LivroListPresenter(Frame parent,
                              LivroListView view,
                              BookSearchUseCase searchUseCase,
                              BookCrudUseCase crudUseCase,
                              BookExportUseCase exportUseCase,
                              ImportUseCase importUseCase,
                              IsbnLookupUseCase isbnLookupUseCase) {
        this.parent            = parent;
        this.view              = view;
        this.searchUseCase     = searchUseCase;
        this.crudUseCase       = crudUseCase;
        this.exportUseCase     = exportUseCase;
        this.importUseCase     = importUseCase;
        this.isbnLookupUseCase = isbnLookupUseCase;
        registerCallbacks();
    }

    public void loadLivros() {
        try {
            view.showLivros(searchUseCase.findAll());
        } catch (Exception e) {
            view.showErrorMessage("Erro ao carregar livros: " +
                    e.getMessage());
        }
    }

    private void registerCallbacks() {
        view.onSearch(this::search);
        view.onDelete(this::delete);
        view.onCreate(this::openCreateForm);
        view.onEdit(this::openEditForm);
        view.onImport(this::importFile);
        view.onExport(this::exportFile);
    }

    private void search() {
        try {
            String raw = view.getSearchTerm();
            String term = raw != null ? raw.trim() : "";
            if (term.isEmpty()) {
                view.showLivros(searchUseCase.findAll());
                return;
            }
            view.showLivros(searchUseCase.findByFiltro(term));
        } catch (Exception e) {
            view.showErrorMessage("Erro ao pesquisar: " + e.getMessage());
        }
    }
    private void delete() {
        List<Livro> selected = view.getSelectedLivros();
        if (selected.isEmpty()) {
            view.showErrorMessage("Selecione ao menos um livro para excluir.");
            return;
        }
        String message = selected.size() == 1
                ? "Excluir \"" + selected.get(0).getTitulo() + "\"?"
                : "Excluir " + selected.size() + " livro(s) selecionado(s)?";
        if (!view.confirm(message)) return;

        int errors = 0;
        for (Livro livro : selected) {
            try {
                crudUseCase.delete(livro.getId());
            } catch (Exception e) {
                errors++;
            }
        }
        search();
        if (errors > 0) {
            view.showErrorMessage(errors + " livro(s) não puderam ser excluídos.");
        }
    }

    private void importFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Arquivos de importação (CSV, XML)", "csv", "xml"));
        if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        try (FileInputStream is = new FileInputStream(file)) {
            List<ImportPreviewRecord> previews = importUseCase.preview(is, file.getName());
            ImportPreviewDialog dialog = new ImportPreviewDialog(parent, previews);
            dialog.setVisible(true);

            if (!dialog.isConfirmed()) return;

            new SwingWorker<ImportResult, Void>() {
                @Override
                protected ImportResult doInBackground() throws Exception {
                    return importUseCase.importSelected(previews);
                }

                @Override
                protected void done(){
                    try{
                        ImportResult result = importUseCase.importSelected(previews);
                        loadLivros();
                        showImportResult(result);
                    } catch (Exception e) {
                        view.showErrorMessage("Erro ao importar: " + e.getMessage());
                    }
                }

            }.execute();


        } catch (Exception e) {
            view.showErrorMessage("Erro ao importar: " + e.getMessage());
        }
    }

    private void showImportResult(ImportResult result) {
        StringBuilder sb = new StringBuilder();
        if (result.getTotalNew() > 0) {
            sb.append(result.getTotalNew()).append(" livro(s) importado(s) com sucesso.");
        }
        if (result.getTotalSkipped() > 0) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(result.getTotalSkipped()).append(" livro(s) já existiam no acervo e não foram importados.");
        }
        if (sb.length() == 0) {
            sb.append("Nenhum livro foi importado.");
        }
        if (!result.hasErrors()) {
            JOptionPane.showMessageDialog(parent, sb.toString(), "Importação concluída", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        sb.append("\n\n").append(result.getErrors().size()).append(" erro(s) encontrado(s):\n");
        for (String error : result.getErrors()) {
            sb.append("\n• ").append(error);
        }
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(560, 240));
        JOptionPane.showMessageDialog(parent, scroll, "Importação concluída com erros", JOptionPane.WARNING_MESSAGE);
    }

    private void exportFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Arquivo CSV (*.csv)", "csv"));
        chooser.setSelectedFile(new File("acervo.csv"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }
        if (file.exists()) {
            int confirm = JOptionPane.showConfirmDialog(
                    parent,
                    "O arquivo \"" + file.getName() + "\" já existe. Deseja substituí-lo?",
                    "Confirmar substituição",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) return;
        }
        try (OutputStreamWriter writer = new OutputStreamWriter(
                Files.newOutputStream(file.toPath()),
                StandardCharsets.UTF_8)
        ) {
            int total = exportUseCase.exportToCsv(writer);
            JOptionPane.showMessageDialog(parent,
                    total + " livro(s) exportado(s) para " + file.getName(),
                    "Exportação concluída", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            view.showErrorMessage("Erro ao exportar: " + e.getMessage());
        }
    }

    private void openCreateForm() {
        LivroFormDialog dialog = new LivroFormDialog(parent);
        LivroFormPresenter presenter = new LivroFormPresenter(dialog, crudUseCase, searchUseCase, isbnLookupUseCase, this::loadLivros);
        presenter.initCreate();
        dialog.setVisible(true);
    }

    private void openEditForm() {
        Optional<Livro> selected = view.getSelectedLivro();
        if (!selected.isPresent()) {
            return;
        }
        Optional<Livro> livroCompleto = crudUseCase.findById(selected.get().getId());
        if (!livroCompleto.isPresent()) {
            view.showErrorMessage("Livro não encontrado. A lista será recarregada");
            loadLivros();
            return;
        }
        LivroFormDialog dialog = new LivroFormDialog(parent);
        LivroFormPresenter presenter = new LivroFormPresenter(dialog, crudUseCase, searchUseCase, isbnLookupUseCase, this::loadLivros);
        presenter.initEdit(livroCompleto.get());
        dialog.setVisible(true);
    }
}
