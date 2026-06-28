package br.com.yuri.alpha7.infra.client.openlibrary;

import br.com.yuri.alpha7.domain.livro.model.Livro;

import java.io.Serializable;
import java.util.Optional;

/**
 * Value type que encapsula o resultado de uma consulta à OpenLibrary armazenado em cache.
 *
 * <p>Criado via factory methods estáticos:
 * <ul>
 *   <li>{@link #found(br.com.yuri.alpha7.domain.livro.model.Livro)} — quando um livro foi encontrado.</li>
 *   <li>{@link #notFound()} — reservado para futuro uso; atualmente o {@link CachingOpenLibraryClient}
 *       não armazena resultados negativos em cache.</li>
 * </ul>
 *
 * <p>Implementa {@link Serializable} porque o Ehcache pode serializar entradas para disco
 * (cache persistente ou tiered) dependendo da configuração em {@code ehcache-isbn-cache.xml}.
 */
public class CachedBookLookup implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean found;
    private final Livro livro;

    private CachedBookLookup(boolean found, Livro livro) {
        this.found = found;
        this.livro = livro;
    }

    public static CachedBookLookup found(Livro livro){
        return new CachedBookLookup(true,livro);
    }

    public static CachedBookLookup notFound(){
        return new CachedBookLookup(false,null);
    }

    public Optional<Livro> toOptional() {
        if(found) {
            return Optional.of(livro);
        }
        return Optional.empty();
    }
}
