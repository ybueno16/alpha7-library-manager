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

    public LivroRepository livroRepository() {
        return livroRepository;
    }

    public AutorRepository autorRepository() {
        return autorRepository;
    }

    public EditoraRepository editoraRepository() {
        return editoraRepository;
    }

    public UnitOfWork unitOfWork() {
        return unitOfWork;
    }

    public void shutdown() {
        HibernateUtil.shutdown();
    }
}