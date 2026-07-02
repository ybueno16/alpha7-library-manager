package br.com.yuri.alpha7.presentation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Janela inicial exibida enquanto a aplicação desktop inicializa infraestrutura e UI.
 *
 * <p>A splash screen evita que o usuário veja uma janela vazia durante operações
 * como configuração do tema, criação dos casos de uso e abertura da janela principal.</p>
 */
public class SplashScreenWindow extends JWindow {

    /**
     * Monta a splash screen com título, subtítulo e progresso indeterminado.
     */
    public SplashScreenWindow() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel icon = new JLabel("📚");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Alpha7 Library Manager");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Sistema de Gerenciamento de Acervo");
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        subtitle.setForeground(Color.GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(icon);
        panel.add(Box.createVerticalStrut(15));
        panel.add(title);
        panel.add(Box.createVerticalStrut(8));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(25));

        JProgressBar progress = new JProgressBar();
        progress.setIndeterminate(true);
        progress.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(progress);
        panel.add(Box.createVerticalStrut(12));

        JLabel loading = new JLabel("Inicializando aplicação...");
        loading.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        loading.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(loading);

        setContentPane(panel);

        setSize(500, 280);
        setLocationRelativeTo(null);
    }
}
