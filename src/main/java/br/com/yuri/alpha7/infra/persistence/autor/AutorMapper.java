package br.com.yuri.alpha7.infra.persistence.autor;

import br.com.yuri.alpha7.domain.autor.model.Autor;

/**
 * Utilitário estático para conversão bidirecional entre {@link AutorEntity} (camada JPA)
 * e {@link br.com.yuri.alpha7.domain.autor.model.Autor} (domínio).
 *
 * <p>A separação entre entidade JPA e modelo de domínio garante que o domínio permaneça
 * livre de anotações de framework. Este mapper é o único ponto de acoplamento entre as duas
 * representações. Ambos os métodos retornam {@code null} quando recebem {@code null},
 * permitindo chamadas seguras com valores opcionais.
 */
public class AutorMapper {

    private AutorMapper() {}

    /**
     * Converte a entidade JPA para o objeto de domínio.
     *
     * @param entity entidade a converter, ou {@code null}
     * @return autor de domínio correspondente, ou {@code null} se {@code entity} for {@code null}
     */
    public static Autor toDomain(AutorEntity entity) {
        if (entity == null) {
            return null;
        }
        Autor autor = new Autor(entity.getNome());
        autor.setId(entity.getId());
        autor.setDataNascimento(entity.getDataNascimento());
        autor.setDataFalecimento(entity.getDataFalecimento());
        autor.setBio(entity.getBio());
        return autor;
    }

    /**
     * Converte o objeto de domínio para entidade JPA.
     *
     * @param domain autor de domínio a converter, ou {@code null}
     * @return entidade JPA correspondente, ou {@code null} se {@code domain} for {@code null}
     */
    public static AutorEntity toEntity(Autor domain) {
        if (domain == null) {
            return null;
        }
        AutorEntity entity = new AutorEntity();
        entity.setId(domain.getId());
        entity.setNome(domain.getNome());
        entity.setDataNascimento(domain.getDataNascimento());
        entity.setDataFalecimento(domain.getDataFalecimento());
        entity.setBio(domain.getBio());
        return entity;
    }
}
