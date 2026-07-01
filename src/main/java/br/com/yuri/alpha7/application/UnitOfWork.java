package br.com.yuri.alpha7.application;

import java.util.function.Supplier;

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

    /**
     * Executa a ação dentro de uma transação e retorna o resultado.
     * Em caso de erro, a transação é revertida automaticamente.
     *
     * @param action operação a ser executada transacionalmente
     * @return resultado produzido pela ação
     */
    <T> T execute(Supplier<T> action);
}
