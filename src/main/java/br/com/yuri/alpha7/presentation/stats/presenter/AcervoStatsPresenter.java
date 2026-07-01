package br.com.yuri.alpha7.presentation.stats.presenter;

import br.com.yuri.alpha7.application.stats.AcervoStats;
import br.com.yuri.alpha7.application.stats.AcervoStatsUseCase;
import br.com.yuri.alpha7.presentation.stats.view.AcervoStatsView;

import javax.swing.SwingWorker;

/**
 * Presenter responsável por coordenar a tela de estatísticas do acervo.
 *
 * <p>Conecta o {@link AcervoStatsUseCase} à {@link AcervoStatsView}: carrega os dados
 * agregados e os repassa para a view renderizar. O botão "Atualizar" da view está vinculado
 * a {@link #load()}, permitindo que o usuário atualize os números após cadastrar ou importar livros.
 */
public class AcervoStatsPresenter {

    private final AcervoStatsView    view;
    private final AcervoStatsUseCase useCase;

    public AcervoStatsPresenter(AcervoStatsView view, AcervoStatsUseCase useCase) {
        this.view    = view;
        this.useCase = useCase;
        view.onRefresh(this::load);
    }

    /**
     * Carrega as estatísticas do acervo e as exibe na view.
     *
     * <p>Deve ser chamado ao exibir a aba de estatísticas pela primeira vez e sempre que
     * o usuário acionar o botão "Atualizar".
     */
    public void load() {
        view.setLoading(true);
        new SwingWorker<AcervoStats, Void>() {
            @Override
            protected AcervoStats doInBackground() {
                return useCase.getAcervo();
            }

            @Override
            protected void done() {
                view.setLoading(false);
                try {
                    view.showStats(get());
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    view.showErrorMessage("Erro ao carregar estatísticas: " + cause.getMessage());
                }
            }
        }.execute();
    }
}
