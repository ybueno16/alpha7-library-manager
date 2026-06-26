package br.com.yuri.alpha7.presentation;

/**
 * Contrato para telas de listagem com operações CRUD.
 * Estende {@link View} adicionando callbacks para criação, edição e exclusão.
 */
public interface CrudView extends View {

    /**
     * Registra a ação a ser executada ao clicar em "Novo".
     *
     * @param acao ação de criação
     */
    void onCreate(Runnable acao);

    /**
     * Registra a ação a ser executada ao clicar em "Editar".
     *
     * @param acao ação de edição
     */
    void onEdit(Runnable acao);

    /**
     * Registra a ação a ser executada ao clicar em "Excluir".
     *
     * @param acao ação de exclusão
     */
    void onDelete(Runnable acao);
}
