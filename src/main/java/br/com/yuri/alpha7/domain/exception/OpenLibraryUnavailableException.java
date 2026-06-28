package br.com.yuri.alpha7.domain.exception;

/**
 * Lançada quando a API da OpenLibrary retorna um status de erro (não-2xx, exceto 404) ou
 * quando ocorre uma falha de I/O na chamada HTTP.
 *
 * <p>Diferentemente de "livro não encontrado" (que resulta em {@code Optional.empty()}),
 * esta exceção indica que o serviço está indisponível ou degradado. O {@code CachingOpenLibraryClient}
 * propaga esta exceção sem armazenar nada em cache, permitindo que a próxima tentativa consulte
 * o serviço novamente.
 *
 * <p>Esta é a única exceção de domínio que NÃO herda de {@link LibraryException}, pois
 * representa uma falha de infraestrutura externa, não uma violação de regra de negócio.
 */
public class OpenLibraryUnavailableException extends RuntimeException {
    public OpenLibraryUnavailableException(String message) {
        super(message);
    }
}
