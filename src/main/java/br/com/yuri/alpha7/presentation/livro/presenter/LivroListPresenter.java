package br.com.yuri.alpha7.presentation.livro.presenter;

import br.com.yuri.alpha7.application.importacao.ImportResult;
import br.com.yuri.alpha7.application.importacao.ImportUseCase;
import br.com.yuri.alpha7.application.isbn.IsbnLookupUseCase;
import br.com.yuri.alpha7.application.livro.BookCrudUseCase;
import br.com.yuri.alpha7.application.livro.BookSearchUseCase;
import br.com.yuri.alpha7.domain.editora.repository.EditoraRepository;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.presentation.livro.view.LivroFormDialog;
import br.com.yuri.alpha7.presentation.livro.view.LivroListView;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.FileInputStream;
import java.util.Optional;

public class LivroListPresenter {

    private final LivroListView     view;
    private final BookSearchUseCase searchUseCase;
    private final BookCrudUseCase   crudUseCase;
    private final ImportUseCase     importUseCase;
    private final IsbnLookupUseCase isbnLookupUseCase;
    private final EditoraRepository editoraRepository;

    public LivroListPresenter(LivroListView view,
                              BookSearchUseCase searchUseCase,
                              BookCrudUseCase crudUseCase,
                              ImportUseCase importUseCase,
                              IsbnLookupUseCase isbnLookupUseCase,
                              EditoraRepository editoraRepository) {
        this.view              = view;
        this.searchUseCase     = searchUseCase;
        this.crudUseCase       = crudUseCase;
        this.importUseCase     = importUseCase;
        this.isbnLookupUseCase = isbnLookupUseCase;
        this.editoraRepository = editoraRepository;
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
        view.onImport(this::importCsv);
    }

    private void search() {
        String term = view.getSearchTerm().trim();
        if (term.isEmpty()) {
            view.showLivros(searchUseCase.findAll());
        } else {
            view.showLivros(searchUseCase.findByFiltro(term));
        }
    }

    private void delete() {
        Optional<Livro> selected = view.getSelectedLivro();
        if (!selected.isPresent()) {
            view.showErrorMessage("Selecione um livro para excluir.");
            return;
        }
        Livro livro = selected.get();
        int confirm = JOptionPane.showConfirmDialog(
                null,
                "Excluir \"" + livro.getTitulo() + "\"?",
                "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            crudUseCase.delete(livro.getId());
            loadLivros();
        } catch (Exception e) {
            view.showErrorMessage("Erro ao excluir: " + e.getMessage());
        }
    }

    private void importCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("CSV", "csv"));
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;
        try (FileInputStream is = new FileInputStream(chooser.getSelectedFile())) {
            ImportResult result = importUseCase.importCsv(is);
            loadLivros();
            JOptionPane.showMessageDialog(null,
                    "Importados: " + result.getTotalSaved() + " | Erros: " + result.getErrors().size());
        } catch (Exception e) {
            view.showErrorMessage("Erro ao importar: " + e.getMessage());
        }
    }

    private void openCreateForm() {
        LivroFormDialog dialog = new LivroFormDialog(null);
        LivroFormPresenter presenter = new LivroFormPresenter(dialog, crudUseCase, isbnLookupUseCase, editoraRepository, this::loadLivros);
        presenter.initCreate();
        dialog.setVisible(true);
    }

    private void openEditForm() {
        Optional<Livro> selected = view.getSelectedLivro();
        if (!selected.isPresent()) return;
        LivroFormDialog dialog = new LivroFormDialog(null);
        LivroFormPresenter presenter = new LivroFormPresenter(dialog, crudUseCase, isbnLookupUseCase, editoraRepository, this::loadLivros);
        presenter.initEdit(selected.get());
        dialog.setVisible(true);
    }
}
