package br.com.yuri.alpha7.application.editora;

import br.com.yuri.alpha7.domain.editora.model.Editora;
import br.com.yuri.alpha7.domain.editora.repository.EditoraRepository;

/**
 * Caso de uso responsável pelas operações sobre editoras.
 *
 * <p>O método principal, {@link #findOrCreate(String)}, encapsula o padrão de upsert por nome
 * que aparece tanto no formulário de livros quanto na importação de CSV: antes de criar uma
 * nova editora, o sistema verifica se já existe uma com o mesmo nome e reutiliza o registro
 * existente. Isso evita duplicatas causadas por cadastros com nomes idênticos.
 */
public class EditoraUseCase {

    private final EditoraRepository editoraRepository;

    public EditoraUseCase(EditoraRepository editoraRepository) {
        this.editoraRepository = editoraRepository;
    }

    /**
     * Retorna a editora com o nome informado, criando-a caso ainda não exista.
     *
     * @param nome nome da editora
     * @return editora existente ou recém-criada, com {@code id} preenchido
     */
    public Editora findOrCreate(String nome) {
        return editoraRepository.findByNome(nome)
                .orElseGet(() -> editoraRepository.save(new Editora(nome)));
    }
}
