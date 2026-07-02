package br.com.yuri.alpha7.presentation.stats.view;

import br.com.yuri.alpha7.application.stats.AcervoStats;
import br.com.yuri.alpha7.presentation.View;

/**
 * Contrato do painel de estatísticas do acervo no padrão MVP.
 *
 * <p>O {@link br.com.yuri.alpha7.presentation.stats.presenter.AcervoStatsPresenter} depende
 * exclusivamente desta interface, permitindo testar a lógica de carregamento sem instanciar
 * componentes Swing.
 */
public interface AcervoStatsView extends View {

    /**
     * Exibe as estatísticas calculadas do acervo.
     *
     * @param stats estatísticas a serem exibidas
     */
    void showStats(AcervoStats stats);

    /**
     * Registra a ação a ser executada quando o usuário acionar "Atualizar".
     *
     * @param action ação de atualização
     */
    void onRefresh(Runnable action);

    /**
     * Alterna o indicador de carregamento exibido durante o recálculo das estatísticas.
     *
     * @param loading {@code true} para exibir o indicador, {@code false} para ocultá-lo
     */
    void setLoading(boolean loading);
}
