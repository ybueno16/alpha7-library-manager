package br.com.yuri.alpha7.presentation.stats.view;

import br.com.yuri.alpha7.application.stats.AcervoStats;
import br.com.yuri.alpha7.presentation.View;

public interface AcervoStatsView extends View {
    void showStats(AcervoStats stats);
    void onRefresh(Runnable action);
    void setLoading(boolean loading);
}
