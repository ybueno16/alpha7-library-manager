package br.com.yuri.alpha7.presentation;

import br.com.yuri.alpha7.config.UseCaseConfig;
import br.com.yuri.alpha7.presentation.livro.presenter.LivroListPresenter;
import br.com.yuri.alpha7.presentation.livro.view.LivroListPanel;

import javax.swing.*;

public class MainWindow extends JFrame {

    public MainWindow(UseCaseConfig useCases) {
        super("Alpha7 Library Manager");

        LivroListPanel panel = new LivroListPanel();
        LivroListPresenter presenter = new LivroListPresenter(
                panel,
                useCases.bookSearch(),
                useCases.bookCrud(),
                useCases.importUseCase(),
                useCases.isbnLookup(),
                useCases.editoraUseCase()
        );

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        add(panel);

        presenter.loadLivros();
    }
}
