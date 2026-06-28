package br.com.yuri.alpha7.application.importacao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Resultado de uma operação de importação em lote de livros.
 *
 * <p>Cada linha processada incrementa um dos três contadores:
 * <ul>
 *   <li>{@code totalNew} — livro inserido com sucesso (ISBN não existia no acervo).</li>
 *   <li>{@code totalSkipped} — livro ignorado porque o ISBN já consta no acervo ativo.</li>
 *   <li>{@code errors} — linhas que falharam, com mensagem descritiva.</li>
 * </ul>
 *
 * <p>O resultado é apresentado ao usuário ao final da importação, permitindo identificar
 * quais registros precisam de correção sem precisar reprocessar o arquivo inteiro.
 */
public class ImportResult {

    private int totalNew;
    private int totalSkipped;
    private final List<String> errors = new ArrayList<>();

    public void incrementNew() {
        totalNew++;
    }

    public void incrementSkipped() {
        totalSkipped++;
    }

    public void addError(String message) {
        errors.add(message);
    }

    public int getTotalNew() {
        return totalNew;
    }

    public int getTotalSkipped() {
        return totalSkipped;
    }

    public int getTotalSaved() {
        return totalNew;
    }

    public int getTotalProcessed() {
        return totalNew + totalSkipped + errors.size();
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
