package br.com.yuri.alpha7.infra.persistence.livro;

import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.infra.persistence.autor.AutorMapper;
import br.com.yuri.alpha7.infra.persistence.editora.EditoraMapper;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class LivroMapper {

    private LivroMapper() {}

    public static Livro toDomain(LivroEntity entity) {
        if (entity == null) {
            return null;
        }
        Livro livro = new Livro();
        livro.setId(entity.getId());
        livro.setTitulo(entity.getTitulo());
        livro.setIsbn(entity.getIsbn());
        livro.setDataPublicacao(entity.getDataPublicacao());
        livro.setNumeroPaginas(entity.getNumeroPaginas());
        livro.setIdioma(entity.getIdioma());
        livro.setEditora(EditoraMapper.toDomain(entity.getEditora()));
        livro.setAutores(entity.getAutores().stream()
                .map(AutorMapper::toDomain)
                .collect(Collectors.toList()));
        livro.setLivrosSemelhantes(new ArrayList<>());
        return livro;
    }

    public static LivroEntity toEntity(Livro livro) {
        if (livro == null) {
            return null;
        }
        LivroEntity entity = new LivroEntity();
        entity.setId(livro.getId());
        entity.setTitulo(livro.getTitulo());
        entity.setIsbn(livro.getIsbn());
        entity.setDataPublicacao(livro.getDataPublicacao());
        entity.setNumeroPaginas(livro.getNumeroPaginas());
        entity.setIdioma(livro.getIdioma());
        entity.setEditora(EditoraMapper.toEntity(livro.getEditora()));
        entity.setAutores(livro.getAutores().stream()
                .map(AutorMapper::toEntity)
                .collect(Collectors.toList()));
        return entity;
    }
}
