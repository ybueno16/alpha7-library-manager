package br.com.yuri.alpha7.infra.persistence.editora;

import br.com.yuri.alpha7.domain.editora.model.Editora;

public class EditoraMapper {

    private EditoraMapper() {}

    public static Editora toDomain(EditoraEntity entity) {
        if (entity == null) {
            return null;
        }
        Editora editora = new Editora(entity.getNome());
        editora.setId(entity.getId());
        return editora;
    }

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
