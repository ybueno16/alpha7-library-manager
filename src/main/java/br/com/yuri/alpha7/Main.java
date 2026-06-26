package br.com.yuri.alpha7;

import br.com.yuri.alpha7.config.InfrastructureConfig;
import br.com.yuri.alpha7.config.RepositoryConfig;
import br.com.yuri.alpha7.config.UseCaseConfig;
import br.com.yuri.alpha7.presentation.MainWindow;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        FlatDarkLaf.setup();

        InfrastructureConfig infra = new InfrastructureConfig();
        RepositoryConfig repos = new RepositoryConfig();
        UseCaseConfig useCases = new UseCaseConfig(infra, repos);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            repos.shutdown();
            infra.shutdown();
        }));

        SwingUtilities.invokeLater(() -> new MainWindow(useCases).setVisible(true));
    }
}