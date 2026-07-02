package br.com.yuri.alpha7;

import br.com.yuri.alpha7.config.InfrastructureConfig;
import br.com.yuri.alpha7.config.RepositoryConfig;
import br.com.yuri.alpha7.config.UseCaseConfig;
import br.com.yuri.alpha7.presentation.MainWindow;
import br.com.yuri.alpha7.presentation.SplashScreenWindow;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class Main {

    public static void main(String[] args) {
        FlatDarkLaf.setup();

        SwingUtilities.invokeLater(() -> {
            SplashScreenWindow splash = new SplashScreenWindow();
            splash.setVisible(true);

            new SwingWorker<MainWindow, Void>() {
                private InfrastructureConfig infra;
                private RepositoryConfig repos;

                @Override
                protected MainWindow doInBackground() {
                    infra = new InfrastructureConfig();
                    repos = new RepositoryConfig();
                    UseCaseConfig useCases = new UseCaseConfig(infra, repos);

                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        repos.shutdown();
                        infra.shutdown();
                    }));

                    return new MainWindow(useCases);
                }

                @Override
                protected void done() {
                    try {
                        MainWindow window = get();

                        splash.dispose();
                        window.setVisible(true);
                    } catch (Exception e) {
                        splash.dispose();

                        JOptionPane.showMessageDialog(
                                null,
                                "Erro ao iniciar aplicação: " + e.getMessage(),
                                "Erro",
                                JOptionPane.ERROR_MESSAGE
                        );

                        if (repos != null) {
                            repos.shutdown();
                        }

                        if (infra != null) {
                            infra.shutdown();
                        }

                        System.exit(1);
                    }
                }
            }.execute();
        });
    }
}