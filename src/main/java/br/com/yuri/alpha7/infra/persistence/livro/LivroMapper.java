package br.com.yuri.alpha7.infra.persistence.livro;

import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.infra.persistence.autor.AutorMapper;
import br.com.yuri.alpha7.infra.persistence.editora.EditoraMapper;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Converte entre o objeto de domínio {@link br.com.yuri.alpha7.domain.livro.model.Livro}
 * e a entidade JPA {@link LivroEntity}.
 *
 * <p><strong>Restrição de sessão em {@link #toDomain(LivroEntity)}</strong> — a conversão
 * de domínio acessa as coleções lazy ({@code autores} e {@code editora}) da entidade JPA.
 * Por isso, este método <em>deve</em> ser chamado enquanto a sessão Hibernate ainda está
 * aberta, ou seja, dentro do lambda passado a
 * {@link br.com.yuri.alpha7.infra.persistence.BaseRepository#executeInTransaction} ou
 * {@link br.com.yuri.alpha7.infra.persistence.BaseRepository#executeQuery}. Chamar
 * {@code toDomain} fora da sessão resulta em {@code LazyInitializationException}.
 *
 * <p>A lista {@code livrosSemelhantes} é intencionalmente ignorada na conversão para domínio
 * (retorna lista vazia). O relacionamento existe no banco mas não é carregado nas operações
 * da UI para evitar N+1 queries em cascata.
 */
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

    /**
     * Converte a entidade para domínio incluindo a lista de livros semelhantes.
     * Deve ser chamado dentro de uma sessão Hibernate aberta.
     * Os semelhantes são mapeados sem recursão (suas próprias listas ficam vazias).
     */
    public static Livro toDomainWithSemelhantes(LivroEntity entity) {
        if (entity == null) {
            return null;
        }
        Livro livro = toDomain(entity);
        livro.setLivrosSemelhantes(entity.getLivrosSemelhantes().stream()
                .filter(s -> !s.isDeleted())
                .map(LivroMapper::toDomain)
                .collect(Collectors.toList()));
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
