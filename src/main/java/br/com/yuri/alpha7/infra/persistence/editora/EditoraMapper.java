package br.com.yuri.alpha7.infra.persistence.editora;

import br.com.yuri.alpha7.domain.editora.model.Editora;

/**
 * Utilitário estático para conversão bidirecional entre {@link EditoraEntity} (camada JPA)
 * e {@link br.com.yuri.alpha7.domain.editora.model.Editora} (domínio).
 *
 * <p>Segue o mesmo contrato do {@link br.com.yuri.alpha7.infra.persistence.autor.AutorMapper}:
 * ambos os métodos retornam {@code null} quando recebem {@code null}, e a classe é não-instanciável
 * por ter o construtor privado.
 */
public class EditoraMapper {

    private EditoraMapper() {}

    /**
     * Converte a entidade JPA para o objeto de domínio.
     *
     * @param entity entidade a converter, ou {@code null}
     * @return editora de domínio correspondente, ou {@code null} se {@code entity} for {@code null}
     */
    public static Editora toDomain(EditoraEntity entity) {
        if (entity == null) {
            return null;
        }
        Editora editora = new Editora(entity.getNome());
        editora.setId(entity.getId());
        return editora;
    }

    /**
     * Converte o objeto de domínio para entidade JPA.
     *
     * @param domain editora de domínio a converter, ou {@code null}
     * @return entidade JPA correspondente, ou {@code null} se {@code domain} for {@code null}
     */
    public static EditoraEntity toEntity(Editora domain) {
        if (domain == null) {
            return null;
        }
        EditoraEntity entity = new EditoraEntity();
        entity.setId(domain.getId());
        entity.setNome(domain.getNome());
        return entity;
    }
}
