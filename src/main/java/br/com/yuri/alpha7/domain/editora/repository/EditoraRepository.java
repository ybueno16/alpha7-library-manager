package br.com.yuri.alpha7.domain.editora.repository;

import br.com.yuri.alpha7.domain.editora.model.Editora;

import java.util.List;
import java.util.Optional;

public interface EditoraRepository {

    Editora save(Editora editora);

    Optional<Editora> findById(Long id);

    Optional<Editora> findByNome(String nome);

    List<Editora> findAll();

    void delete(Long id);
}
