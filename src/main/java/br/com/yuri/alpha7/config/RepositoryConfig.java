package br.com.yuri.alpha7.config;

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

    public LivroRepositoryImpl livroRepository() { return livroRepository; }
    public AutorRepositoryImpl autorRepository() { return autorRepository; }
    public EditoraRepositoryImpl editoraRepository() { return editoraRepository; }
    public HibernateUnitOfWork unitOfWork() { return unitOfWork; }

    public void shutdown() {
        HibernateUtil.shutdown();
    }
}
