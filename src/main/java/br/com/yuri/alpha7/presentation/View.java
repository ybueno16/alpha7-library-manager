package br.com.yuri.alpha7.presentation;

/**
 * Contrato base para todas as telas da aplicação.
 * Define comportamentos comuns a qualquer view no padrão MVP.
 */
public interface View {

    /**
     * Exibe uma mensagem de erro ao usuário.
     *
     * @param message mensagem de erro
     */
    void showErrorMessage(String message);
}
