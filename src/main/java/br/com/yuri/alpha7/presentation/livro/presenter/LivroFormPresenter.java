package br.com.yuri.alpha7.presentation.livro.presenter;

import br.com.yuri.alpha7.application.editora.EditoraUseCase;
import br.com.yuri.alpha7.application.isbn.IsbnLookupUseCase;
import br.com.yuri.alpha7.application.livro.BookCrudUseCase;
import br.com.yuri.alpha7.application.livro.BookSearchUseCase;
import br.com.yuri.alpha7.domain.autor.model.Autor;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import br.com.yuri.alpha7.presentation.livro.view.LivroFormView;

import javax.swing.SwingWorker;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Presenter responsável pela lógica do formulário de cadastro e edição de livros.
 *
 * <p>Opera em dois modos, configurados pelo chamador antes de exibir a view:
 * <ul>
 *   <li>{@link #initCreate()} — modo de criação: nenhum id é mantido; o livro será inserido.</li>
 *   <li>{@link #initEdit(br.com.yuri.alpha7.domain.livro.model.Livro)} — modo de edição:
 *       o id original é preservado internamente e atribuído ao livro construído no salvamento,
 *       garantindo que o repositório faça update em vez de insert.</li>
 * </ul>
 *
 * <p>A busca por ISBN é executada de forma assíncrona em um {@link javax.swing.SwingWorker}
 * para não bloquear a EDT. O botão de busca é desabilitado durante a requisição e reabilitado
 * ao término, independentemente de sucesso ou falha.
 *
 * <p>As validações realizadas antes de salvar são: título obrigatório, ISBN obrigatório,
 * ao menos um autor e formato de data (apenas ano {@code yyyy}, se informada). A editora é
 * resolvida pelo nome via {@link br.com.yuri.alpha7.domain.editora.repository.EditoraRepository}:
 * se já existir, é reaproveitada; caso contrário, uma nova editora é criada.
 */
public class LivroFormPresenter {

    private final LivroFormView     view;
    private final BookCrudUseCase   crudUseCase;
    private final BookSearchUseCase searchUseCase;
    private final IsbnLookupUseCase isbnLookupUseCase;
    private final EditoraUseCase    editoraUseCase;
    private final Runnable          onSuccess;
    private Long livroId;

    public LivroFormPresenter(LivroFormView view,
                              BookCrudUseCase crudUseCase,
                              BookSearchUseCase searchUseCase,
                              IsbnLookupUseCase isbnLookupUseCase,
                              EditoraUseCase editoraUseCase,
                              Runnable onSuccess) {
        this.view              = view;
        this.crudUseCase       = crudUseCase;
        this.searchUseCase     = searchUseCase;
        this.isbnLookupUseCase = isbnLookupUseCase;
        this.editoraUseCase    = editoraUseCase;
        this.onSuccess         = onSuccess;
        view.onIsbnLookup(this::lookupByIsbn);
        view.onSave(this::save);
        view.onCancel(view::close);
        view.onAddSemelhante(this::addSemelhante);
        view.onRemoveSemelhante(this::removeSemelhante);
    }

    public void initCreate() {
        this.livroId = null;
    }

    public void initEdit(Livro livro) {
        this.livroId = livro.getId();
        view.setLivro(livro);
        view.setLivrosSemelhantes(livro.getLivrosSemelhantes());
    }

    private void lookupByIsbn() {
        String isbnStr = view.getIsbn();
        if (isbnStr.isEmpty()) {
            view.showValidationError("Digite um ISBN antes de buscar.");
            return;
        }

        ISBN isbn;
        try {
            isbn = new ISBN(isbnStr);
        } catch (Exception e) {
            view.showValidationError("ISBN inválido: " + e.getMessage());
            return;
        }

        view.setLookupEnabled(false);

        new SwingWorker<Optional<Livro>, Void>() {

            @Override
            protected Optional<Livro> doInBackground() {
                return isbnLookupUseCase.findByIsbn(isbn);
            }

            @Override
            protected void done() {
                view.setLookupEnabled(true);
                try {
                    Optional<Livro> result = get();
                    if (result.isPresent()) {
                        view.setLivro(result.get());
                        return;
                    }
                    view.showValidationError("Nenhum livro encontrado para o ISBN informado.");
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    view.showValidationError("Erro ao buscar ISBN: " + cause.getMessage());
                }
            }
        }.execute();
    }

    private void addSemelhante() {
        List<Livro> todos = searchUseCase.findAll();
        List<Livro> atuais = view.getLivrosSemelhantes();
        List<Long> idsAtuais = atuais.stream()
                .map(Livro::getId)
                .collect(Collectors.toList());
        List<Livro> disponiveis = todos.stream()
                .filter(l -> l.getId() != null && !l.getId().equals(livroId))
                .filter(l -> !idsAtuais.contains(l.getId()))
                .collect(Collectors.toList());
        view.pickSemelhante(disponiveis).ifPresent(escolhido -> {
            List<Livro> novos = new ArrayList<>(atuais);
            novos.add(escolhido);
            view.setLivrosSemelhantes(novos);
        });
    }

    private void removeSemelhante() {
        view.getSelectedSemelhante().ifPresent(semelhante -> {
            List<Livro> atuais = new ArrayList<>(view.getLivrosSemelhantes());
            atuais.remove(semelhante);
            view.setLivrosSemelhantes(atuais);
        });
    }

    private void save() {
        if (!validate()) return;
        try {
            Livro livro = buildLivro();
            crudUseCase.save(livro);
            onSuccess.run();
            view.close();
        } catch (Exception e) {
            view.showErrorMessage("Erro ao salvar: " + e.getMessage());
        }
    }

    private boolean validate() {
        view.clearValidationError();
        if (view.getTitulo().isEmpty()) {
            view.showValidationError("Título é obrigatório.");
            return false;
        }
        if (view.getIsbn().isEmpty()) {
            view.showValidationError("ISBN é obrigatório.");
            return false;
        }
        if (view.getAutores().isEmpty()) {
            view.showValidationError("Informe ao menos um autor.");
            return false;
        }
        String dateStr = view.getDataPublicacao();
        if (!dateStr.isEmpty() && !dateStr.matches("\\d{4}")) {
            view.showValidationError("Publicação inválida. Use apenas o ano (ex: 2003).");
            return false;
        }
        return true;
    }

    private Livro buildLivro() {
        Livro livro = new Livro();

        if (livroId != null) {
            livro.setId(livroId);
        }

        livro.setTitulo(view.getTitulo());
        livro.setIsbn(new ISBN(view.getIsbn()));
        livro.setAutores(parseAutores(view.getAutores()));
        livro.setLivrosSemelhantes(view.getLivrosSemelhantes());

        String editoraStr = view.getEditora();
        if (!editoraStr.isEmpty()) {
            livro.setEditora(editoraUseCase.findOrCreate(editoraStr));
        }

        String dateStr = view.getDataPublicacao();
        if (!dateStr.isEmpty()) {
            livro.setDataPublicacao(parsePublishDate(dateStr));
        }

        String idioma = view.getIdioma();
        if (!idioma.isEmpty()) {
            livro.setIdioma(idioma);
        }

        String pagesStr = view.getNumeroPaginas();
        if (!pagesStr.isEmpty()) {
            livro.setNumeroPaginas(Integer.parseInt(pagesStr));
        }

        return livro;
    }

    private LocalDate parsePublishDate(String dateStr) {
        if (dateStr.matches("\\d{4}")) {
            return LocalDate.of(Integer.parseInt(dateStr), 1, 1);
        }
        return LocalDate.parse(dateStr);
    }

    private List<Autor> parseAutores(String autoresStr) {
        return Arrays.stream(autoresStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Autor::new)
                .collect(Collectors.toList());
    }
}
