package br.com.yuri.alpha7.presentation.livro.presenter;

import br.com.yuri.alpha7.application.importacao.model.ImportPreviewRecord;
import br.com.yuri.alpha7.application.importacao.model.ImportResult;
import br.com.yuri.alpha7.application.importacao.ImportUseCase;
import br.com.yuri.alpha7.application.isbn.IsbnLookupUseCase;
import br.com.yuri.alpha7.application.livro.BookCrudUseCase;
import br.com.yuri.alpha7.application.livro.BookExportUseCase;
import br.com.yuri.alpha7.application.livro.BookSearchUseCase;
import br.com.yuri.alpha7.domain.exception.BookNotFoundException;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.repository.LivroFiltro;
import br.com.yuri.alpha7.domain.livro.repository.PagedResult;
import br.com.yuri.alpha7.presentation.livro.view.ImportPreviewDialog;
import br.com.yuri.alpha7.presentation.livro.view.LivroFormDialog;
import br.com.yuri.alpha7.presentation.livro.view.LivroListView;
import br.com.yuri.alpha7.presentation.livro.view.ProgressDialog;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class LivroListPresenter {

    static final int PAGE_SIZE = 50;

    private final Frame            parent;
    private final LivroListView     view;
    private final BookSearchUseCase searchUseCase;
    private final BookCrudUseCase   crudUseCase;
    private final BookExportUseCase exportUseCase;
    private final ImportUseCase     importUseCase;
    private final IsbnLookupUseCase isbnLookupUseCase;

    private int currentPage = 0;

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
        currentPage = 0;
        loadFilterOptions();
        loadPage();
    }

    private void registerCallbacks() {
        view.onSearch(this::search);
        view.onDelete(this::delete);
        view.onCreate(this::openCreateForm);
        view.onEdit(this::openEditForm);
        view.onImport(this::importFile);
        view.onExport(this::exportFile);
        view.onNextPage(this::nextPage);
        view.onPreviousPage(this::previousPage);
    }

    private void search() {
        currentPage = 0;
        loadPage();
    }

    private void nextPage() {
        currentPage++;
        loadPage();
    }

    private void previousPage() {
        if (currentPage > 0) currentPage--;
        loadPage();
    }

    private void loadPage() {
        final int page = currentPage;
        final LivroFiltro filtro = buildFiltro();

        new SwingWorker<PagedResult<Livro>, Void>() {
            @Override
            protected PagedResult<Livro> doInBackground() {
                if (filtro.isEmpty()) {
                    return searchUseCase.findAll(page, PAGE_SIZE);
                }
                return searchUseCase.findByFiltro(filtro, page, PAGE_SIZE);
            }

            @Override
            protected void done() {
                try {
                    PagedResult<Livro> result = get();
                    int totalPages = Math.max(1, result.totalPages(PAGE_SIZE));
                    view.showLivros(result.getItems());
                    view.showPaginationInfo(page + 1, totalPages);
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    view.showErrorMessage("Erro ao carregar livros: " + cause.getMessage());
                }
            }
        }.execute();
    }

    private LivroFiltro buildFiltro() {
        return new LivroFiltro(
                trim(view.getSearchTerm()),
                trim(view.getAutorFiltro()),
                trim(view.getEditoraFiltro()),
                parseYear(view.getAnoDe()),
                parseYear(view.getAnoAte()),
                trim(view.getIdiomaFiltro())
        );
    }

    private void loadFilterOptions() {
        new SwingWorker<List<List<String>>, Void>() {
            @Override
            protected List<List<String>> doInBackground() {
                return Arrays.asList(
                        searchUseCase.findAllEditorasAtivas(),
                        searchUseCase.findAllIdiomasDistintos()
                );
            }

            @Override
            protected void done() {
                try {
                    List<List<String>> result = get();
                    view.setEditoraOptions(result.get(0));
                    view.setIdiomaOptions(result.get(1));
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    private static String trim(String s) {
        return s != null ? s.trim() : "";
    }

    private static Integer parseYear(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
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

        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() {
                List<String> failed = new ArrayList<>();
                for (Livro livro : selected) {
                    try {
                        crudUseCase.delete(livro.getId());
                    } catch (Exception e) {
                        failed.add("\"" + livro.getTitulo() + "\"");
                    }
                }
                return failed;
            }

            @Override
            protected void done() {
                try {
                    List<String> failed = get();
                    loadPage();
                    if (!failed.isEmpty()) {
                        view.showErrorMessage("Não foi possível excluir: " + String.join(", ", failed));
                    }
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    view.showErrorMessage("Erro ao excluir: " + cause.getMessage());
                }
            }
        }.execute();
    }

    private void importFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Arquivos de importação (CSV, XML)", "csv", "xml"));
        if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();

        new SwingWorker<List<ImportPreviewRecord>, Void>() {
            @Override
            protected List<ImportPreviewRecord> doInBackground() throws Exception {
                try (FileInputStream is = new FileInputStream(file)) {
                    return importUseCase.preview(is, file.getName());
                }
            }

            @Override
            protected void done() {
                List<ImportPreviewRecord> previews;
                try {
                    previews = get();
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    view.showErrorMessage("Erro ao ler arquivo: " + cause.getMessage());
                    return;
                }

                ImportPreviewDialog dialog = new ImportPreviewDialog(parent, previews);
                dialog.setVisible(true);

                if (!dialog.isConfirmed()) return;

                int totalImportavel = (int) previews.stream()
                        .filter(p -> p.isSelecionado()
                                && p.getStatus() != ImportPreviewRecord.Status.ERRO)
                        .count();

                ProgressDialog progressDialog = new ProgressDialog(
                        parent,
                        "Importando livros...",
                        totalImportavel,
                        (current, t) -> "Importando " + current + " de " + t + "...");

                SwingWorker<ImportResult, Integer> importWorker =
                        new SwingWorker<ImportResult, Integer>() {
                    @Override
                    protected ImportResult doInBackground() throws Exception {
                        return importUseCase.importSelected(previews, this::publish);
                    }

                    @Override
                    protected void process(java.util.List<Integer> chunks) {
                        progressDialog.update(chunks.get(chunks.size() - 1), totalImportavel);
                    }

                    @Override
                    protected void done() {
                        progressDialog.dispose();
                        try {
                            ImportResult result = get();
                            loadLivros();
                            showImportResult(result);
                        } catch (Exception e) {
                            view.showErrorMessage("Erro ao importar: " + e.getMessage());
                        }
                    }
                };

                importWorker.execute();
                progressDialog.setVisible(true);
            }
        }.execute();
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
            sb.append(result.hasErrors()
                    ? "Nenhum livro foi importado. Verifique os erros abaixo."
                    : "Nenhum livro foi importado.");
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
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }
        final File finalFile = file;
        if (finalFile.exists()) {
            int confirm = JOptionPane.showConfirmDialog(
                    parent,
                    "O arquivo \"" + finalFile.getName() + "\" já existe. Deseja substituí-lo?",
                    "Confirmar substituição",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) return;
        }

        ProgressDialog progressDialog = new ProgressDialog(
                parent,
                "Exportando livros...",
                0,
                (current, total) -> "Exportando " + current + " de " + total + " livros...");

        SwingWorker<Integer, int[]> exportWorker = new SwingWorker<Integer, int[]>() {
            @Override
            protected Integer doInBackground() throws Exception {
                try (OutputStreamWriter writer = new OutputStreamWriter(
                        Files.newOutputStream(finalFile.toPath()), StandardCharsets.UTF_8)) {
                    writer.write('﻿');
                    return exportUseCase.exportToCsv(writer,
                            (current, total) -> publish(new int[]{current, total}));
                }
            }

            @Override
            protected void process(java.util.List<int[]> chunks) {
                int[] last = chunks.get(chunks.size() - 1);
                progressDialog.update(last[0], last[1]);
            }

            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    int total = get();
                    JOptionPane.showMessageDialog(parent,
                            total + " livro(s) exportado(s) para " + finalFile.getName(),
                            "Exportação concluída", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    view.showErrorMessage("Erro ao exportar: " + cause.getMessage());
                }
            }
        };

        exportWorker.execute();
        progressDialog.setVisible(true);
    }

    private void openCreateForm() {
        LivroFormDialog dialog = new LivroFormDialog(parent);
        LivroFormPresenter presenter = new LivroFormPresenter(dialog, crudUseCase, searchUseCase, isbnLookupUseCase, this::loadLivros);
        presenter.initCreate();
        dialog.setVisible(true);
    }

    private void openEditForm() {
        Optional<Livro> selected = view.getSelectedLivro();
        if (!selected.isPresent()) return;

        new SwingWorker<Livro, Void>() {
            @Override
            protected Livro doInBackground() {
                return crudUseCase.findById(selected.get().getId());
            }

            @Override
            protected void done() {
                try {
                    Livro livroCompleto = get();
                    LivroFormDialog dialog = new LivroFormDialog(parent);
                    LivroFormPresenter presenter = new LivroFormPresenter(
                            dialog, crudUseCase, searchUseCase, isbnLookupUseCase,
                            LivroListPresenter.this::loadLivros);
                    presenter.initEdit(livroCompleto);
                    dialog.setVisible(true);
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof BookNotFoundException) {
                        view.showErrorMessage("Livro não encontrado. A lista será recarregada.");
                        loadLivros();
                        return;
                    }
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    view.showErrorMessage("Erro ao abrir livro: " + cause.getMessage());
                } catch (Exception e) {
                    view.showErrorMessage("Erro ao abrir livro: " + e.getMessage());
                }
            }
        }.execute();
    }
}
