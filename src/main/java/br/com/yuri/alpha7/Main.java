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

/**
 * Ponto de entrada da aplicação desktop.
 *
 * <p>Exibe a splash screen imediatamente e realiza o wiring de infraestrutura,
 * repositórios e casos de uso em um {@link SwingWorker}, para que a inicialização
 * do banco de dados e do cache não bloqueie a EDT. A janela principal só é exibida
 * após o wiring terminar; falhas de inicialização são reportadas ao usuário e encerram
 * a aplicação liberando os recursos já alocados.
 */
public class Main {

    /**
     * Inicializa o Look and Feel, exibe a splash screen e monta a aplicação em background.
     *
     * @param args argumentos de linha de comando (não utilizados)
     */
    public static void main(String[] args) {
        FlatDarkLaf.setup();

        SwingUtilities.invokeLater(() -> {
            SplashScreenWindow splash = new SplashScreenWindow();
            splash.setVisible(true);

            new SwingWorker<UseCaseConfig, Void>() {
                private InfrastructureConfig infra;
                private RepositoryConfig repos;

                @Override
                protected UseCaseConfig doInBackground() {
                    infra = new InfrastructureConfig();
                    repos = new RepositoryConfig();
                    UseCaseConfig useCases = new UseCaseConfig(infra, repos);

                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        repos.shutdown();
                        infra.shutdown();
                    }));

                    return useCases;
                }

                @Override
                protected void done() {
                    try {
                        UseCaseConfig useCases = get();
                        MainWindow window = new MainWindow(useCases);

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