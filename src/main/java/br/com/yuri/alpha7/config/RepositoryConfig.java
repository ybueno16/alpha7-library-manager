package br.com.yuri.alpha7.config;

import br.com.yuri.alpha7.application.UnitOfWork;
import br.com.yuri.alpha7.domain.autor.repository.AutorRepository;
import br.com.yuri.alpha7.domain.editora.repository.EditoraRepository;
import br.com.yuri.alpha7.domain.livro.repository.LivroRepository;
import br.com.yuri.alpha7.infra.persistence.HibernateUnitOfWork;
import br.com.yuri.alpha7.infra.persistence.HibernateUtil;
import br.com.yuri.alpha7.infra.persistence.autor.AutorRepositoryImpl;
import br.com.yuri.alpha7.infra.persistence.editora.EditoraRepositoryImpl;
import br.com.yuri.alpha7.infra.persistence.livro.LivroRepositoryImpl;

/**
 * Wiring manual dos repositórios Hibernate e do {@link UnitOfWork} da aplicação.
 *
 * <p>Expõe as implementações de infraestrutura apenas por suas interfaces de domínio,
 * de forma que a camada de aplicação nunca dependa diretamente de Hibernate.
 */
public class RepositoryConfig {

    private final LivroRepositoryImpl livroRepository;
    private final AutorRepositoryImpl autorRepository;
    private final EditoraRepositoryImpl editoraRepository;
    private final HibernateUnitOfWork unitOfWork;

    public RepositoryConfig() {
        this.livroRepository = new LivroRepositoryImpl();
        this.autorRepository = new AutorRepositoryImpl();
        this.editoraRepository = new EditoraRepositoryImpl();
        this.unitOfWork = new HibernateUnitOfWork();
    }

    /**
     * Retorna o repositório de livros.
     *
     * @return instância única do repositório de livros
     */
    public LivroRepository livroRepository() {
        return livroRepository;
    }

    /**
     * Retorna o repositório de autores.
     *
     * @return instância única do repositório de autores
     */
    public AutorRepository autorRepository() {
        return autorRepository;
    }

    /**
     * Retorna o repositório de editoras.
     *
     * @return instância única do repositório de editoras
     */
    public EditoraRepository editoraRepository() {
        return editoraRepository;
    }

    /**
     * Retorna o Unit of Work compartilhado pelos casos de uso.
     *
     * @return instância única do Unit of Work
     */
    public UnitOfWork unitOfWork() {
        return unitOfWork;
    }

    /** Encerra a fábrica de sessões do Hibernate. */
    public void shutdown() {
        HibernateUtil.shutdown();
    }
}