package br.com.yuri.alpha7.infra.client.openlibrary;

import br.com.yuri.alpha7.domain.livro.model.Livro;

import java.io.Serializable;
import java.util.Optional;

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
