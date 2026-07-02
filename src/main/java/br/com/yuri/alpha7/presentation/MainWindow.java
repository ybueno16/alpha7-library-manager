package br.com.yuri.alpha7.presentation;

import br.com.yuri.alpha7.config.UseCaseConfig;
import br.com.yuri.alpha7.presentation.livro.presenter.LivroListPresenter;
import br.com.yuri.alpha7.presentation.livro.view.LivroListPanel;
import br.com.yuri.alpha7.presentation.stats.presenter.AcervoStatsPresenter;
import br.com.yuri.alpha7.presentation.stats.view.AcervoStatsPanel;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

/**
 * Janela principal ({@link JFrame}) da aplicação Alpha7 Library Manager.
 *
 * <p>Organiza as duas telas da aplicação em um {@link JTabbedPane}:
 * <ul>
 *   <li><b>Livros</b> — listagem, busca, cadastro, importação e exportação.</li>
 *   <li><b>Estatísticas</b> — painel com totais e rankings do acervo, recarregado
 *       automaticamente ao selecionar a aba.</li>
 * </ul>
 *
 * <p>O encerramento da janela ({@code EXIT_ON_CLOSE}) aciona o shutdown hook registrado em
 * {@link br.com.yuri.alpha7.Main}, que libera o pool de conexões antes de finalizar a JVM.
 */
public class MainWindow extends JFrame {

    public MainWindow(UseCaseConfig useCases) {
        super("Alpha7 Library Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        LivroListPanel     livrosPanel     = new LivroListPanel();
        LivroListPresenter livrosPresenter = new LivroListPresenter(
                this,
                livrosPanel,
                useCases.bookSearch(),
                useCases.bookCrud(),
                useCases.bookExport(),
                useCases.importUseCase(),
                useCases.isbnLookup()
        );

        AcervoStatsPanel     statsPanel     = new AcervoStatsPanel();
        AcervoStatsPresenter statsPresenter = new AcervoStatsPresenter(
                statsPanel,
                useCases.acervoStats()
        );

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Livros",        livrosPanel);
        tabs.addTab("Estatísticas",  statsPanel);

        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 1) {
                statsPresenter.load();
            }
        });

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        add(tabs);

        livrosPresenter.loadLivros();
    }
}
