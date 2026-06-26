package br.com.yuri.alpha7.application;

/**
 * Abstração de unidade de trabalho transacional.
 * Garante que a ação fornecida seja executada dentro de uma transação atômica.
 */
public interface UnitOfWork {

    /**
     * Executa a ação dentro de uma transação.
     * Em caso de erro, a transação é revertida automaticamente.
     *
     * @param action operação a ser executada transacionalmente
     */
    void execute(Runnable action);
}
