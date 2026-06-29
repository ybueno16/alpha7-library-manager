package br.com.yuri.alpha7.presentation.livro.presenter;

import br.com.yuri.alpha7.application.editora.EditoraUseCase;
import br.com.yuri.alpha7.application.importacao.ImportPreviewRecord;
import br.com.yuri.alpha7.application.importacao.ImportResult;
import br.com.yuri.alpha7.application.importacao.ImportUseCase;
import br.com.yuri.alpha7.application.isbn.IsbnLookupUseCase;
import br.com.yuri.alpha7.application.livro.BookCrudUseCase;
import br.com.yuri.alpha7.application.livro.BookExportUseCase;
import br.com.yuri.alpha7.application.livro.BookSearchUseCase;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.presentation.livro.view.ImportPreviewDialog;
import br.com.yuri.alpha7.presentation.livro.view.LivroFormDialog;
import br.com.yuri.alpha7.presentation.livro.view.LivroListView;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
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

    private final LivroListView     view;
    private final BookSearchUseCase searchUseCase;
    private final BookCrudUseCase   crudUseCase;
    private final BookExportUseCase exportUseCase;
    private final ImportUseCase     importUseCase;
    private final IsbnLookupUseCase isbnLookupUseCase;
    private final EditoraUseCase    editoraUseCase;

    public LivroListPresenter(LivroListView view,
                              BookSearchUseCase searchUseCase,
                              BookCrudUseCase crudUseCase,
                              BookExportUseCase exportUseCase,
                              ImportUseCase importUseCase,
                              IsbnLookupUseCase isbnLookupUseCase,
                              EditoraUseCase editoraUseCase) {
        this.view              = view;
        this.searchUseCase     = searchUseCase;
        this.crudUseCase       = crudUseCase;
        this.exportUseCase     = exportUseCase;
        this.importUseCase     = importUseCase;
        this.isbnLookupUseCase = isbnLookupUseCase;
        this.editoraUseCase    = editoraUseCase;
        registerCallbacks();
    }

    public void loadLivros() {
        view.showLivros(searchUseCase.findAll());
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
        String term = view.getSearchTerm().trim();
        if (term.isEmpty()) {
            view.showLivros(searchUseCase.findAll());
            return;
        }
        view.showLivros(searchUseCase.findByFiltro(term));
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
        loadLivros();
        if (errors > 0) {
            view.showErrorMessage(errors + " livro(s) não puderam ser excluídos.");
        }
    }

    private void importFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Arquivos de importação (CSV, XML)", "csv", "xml"));
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        try (FileInputStream is = new FileInputStream(file)) {
            List<ImportPreviewRecord> previews = importUseCase.preview(is, file.getName());
            ImportPreviewDialog dialog = new ImportPreviewDialog(null, previews);
            dialog.setVisible(true);

            if (!dialog.isConfirmed()) return;

            ImportResult result = importUseCase.importSelected(previews);
            loadLivros();
            showImportResult(result);
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
            JOptionPane.showMessageDialog(null, sb.toString(), "Importação concluída", JOptionPane.INFORMATION_MESSAGE);
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
        JOptionPane.showMessageDialog(null, scroll, "Importação concluída com erros", JOptionPane.WARNING_MESSAGE);
    }

    private void exportFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Arquivo CSV (*.csv)", "csv"));
        chooser.setSelectedFile(new File("acervo.csv"));
        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }
        try (FileWriter writer = new FileWriter(file)) {
            int total = exportUseCase.exportToCsv(writer);
            JOptionPane.showMessageDialog(null,
                    total + " livro(s) exportado(s) para " + file.getName(),
                    "Exportação concluída", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            view.showErrorMessage("Erro ao exportar: " + e.getMessage());
        }
    }

    private void openCreateForm() {
        LivroFormDialog dialog = new LivroFormDialog(null);
        LivroFormPresenter presenter = new LivroFormPresenter(dialog, crudUseCase, searchUseCase, isbnLookupUseCase, editoraUseCase, this::loadLivros);
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
            return;
        }
        LivroFormDialog dialog = new LivroFormDialog(null);
        LivroFormPresenter presenter = new LivroFormPresenter(dialog, crudUseCase, searchUseCase, isbnLookupUseCase, editoraUseCase, this::loadLivros);
        presenter.initEdit(livroCompleto.get());
        dialog.setVisible(true);
    }
}
