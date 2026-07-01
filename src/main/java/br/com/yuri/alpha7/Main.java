package br.com.yuri.alpha7;

import br.com.yuri.alpha7.config.InfrastructureConfig;
import br.com.yuri.alpha7.config.RepositoryConfig;
import br.com.yuri.alpha7.config.UseCaseConfig;
import br.com.yuri.alpha7.presentation.MainWindow;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.SwingUtilities;

/**
 * Ponto de entrada da aplicação Alpha7 Library Manager.
 *
 * <p>Responsabilidades da inicialização:
 * <ol>
 *   <li>Configura o look-and-feel {@link com.formdev.flatlaf.FlatDarkLaf} antes de qualquer
 *       componente Swing ser criado.</li>
 *   <li>Instancia as camadas de infraestrutura, repositório e casos de uso via objetos de
 *       configuração ({@code InfrastructureConfig → RepositoryConfig → UseCaseConfig}).</li>
 *   <li>Registra um shutdown hook para liberar o pool de conexões e o EntityManagerFactory
 *       ao encerrar a JVM.</li>
 *   <li>Abre a janela principal na Event Dispatch Thread via {@link javax.swing.SwingUtilities#invokeLater}.</li>
 * </ol>
 */
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