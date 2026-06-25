package br.com.yuri.alpha7.domain.autor.repository;

import br.com.yuri.alpha7.domain.autor.model.Autor;

import java.util.List;
import java.util.Optional;

public interface AutorRepository {

    Autor save(Autor autor);

    Optional<Autor> findById(Long id);

    Optional<Autor> findByNome(String nome);

    List<Autor> findAll();

    void delete(Long id);
}
