package br.com.yuri.alpha7.domain.exception;

/**
 * Lançada quando ocorre um erro irrecuperável durante a leitura ou processamento do arquivo CSV
 * de importação em lote.
 *
 * <p>Erros em linhas individuais do CSV são tratados internamente pelo {@code ImportUseCase}
 * e registrados no {@code ImportResult} sem interromper o processamento — esta exceção só é
 * lançada para falhas que impedem o início ou a leitura do arquivo como um todo (ex: stream nulo,
 * arquivo mal formatado que impede a análise do cabeçalho).
 */
public class ImportException extends LibraryException {

    public ImportException(String message) {
        super(message);
    }

    public ImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
