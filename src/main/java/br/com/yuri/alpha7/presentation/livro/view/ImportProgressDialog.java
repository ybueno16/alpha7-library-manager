package br.com.yuri.alpha7.presentation.livro.view;

import java.awt.Frame;

/**
 * Especialização do dialog de progresso para a importação de livros.
 */
class ImportProgressDialog extends ProgressDialog {

    /**
     * Cria o dialog de importação com mensagem específica de registros processados.
     *
     * @param parent janela proprietária
     * @param total total de registros selecionados para importação
     */
    ImportProgressDialog(Frame parent, int total) {
        super(parent, "Importando livros...", total,
                (current, t) -> "Importando " + current + " de " + t + " registros...");
    }
}
