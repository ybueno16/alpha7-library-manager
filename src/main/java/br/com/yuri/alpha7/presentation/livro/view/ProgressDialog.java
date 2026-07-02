package br.com.yuri.alpha7.presentation.livro.view;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiFunction;

/**
 * Dialog modal reutilizável para operações longas com progresso.
 *
 * <p>É usado por fluxos como importação e exportação para manter feedback visual
 * enquanto o processamento ocorre em background. Quando o total ainda não é
 * conhecido, a barra inicia em modo indeterminado e passa a determinada no
 * primeiro {@link #update(int, int)} com total válido.</p>
 */
public class ProgressDialog extends JDialog {

    private final JProgressBar                        progressBar;
    private final JLabel                              statusLabel;
    private final BiFunction<Integer, Integer, String> messageFormatter;

    /**
     * Cria um dialog de progresso.
     *
     * @param parent janela proprietária
     * @param title título do dialog
     * @param total total de itens a processar; valores menores ou iguais a zero usam modo indeterminado
     * @param messageFormatter formatador da mensagem de status a partir do progresso atual e total
     */
    public ProgressDialog(Frame parent,
                          String title,
                          int total,
                          BiFunction<Integer, Integer, String> messageFormatter) {
        super(parent, title, true);
        this.messageFormatter = messageFormatter;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(false);

        statusLabel = new JLabel(messageFormatter.apply(0, total));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        progressBar = new JProgressBar(0, total > 0 ? total : 1);
        progressBar.setPreferredSize(new Dimension(400, 26));
        progressBar.setStringPainted(true);

        if (total <= 0) {
            progressBar.setIndeterminate(true);
        } else {
            progressBar.setString("0 / " + total);
        }

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(statusLabel);
        content.add(Box.createVerticalStrut(14));
        content.add(progressBar);

        setContentPane(content);
        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * Atualiza a barra e a mensagem exibida ao usuário.
     *
     * @param current quantidade já processada
     * @param total total de itens a processar
     */
    public void update(int current, int total) {
        if (progressBar.isIndeterminate() && total > 0) {
            progressBar.setIndeterminate(false);
            progressBar.setMaximum(total);
        }
        if (!progressBar.isIndeterminate()) {
            progressBar.setValue(current);
            progressBar.setString(current + " / " + total);
        }
        statusLabel.setText(messageFormatter.apply(current, total));
    }
}
