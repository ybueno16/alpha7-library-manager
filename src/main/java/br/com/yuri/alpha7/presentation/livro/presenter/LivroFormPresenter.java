package br.com.yuri.alpha7.presentation.livro.presenter;

import br.com.yuri.alpha7.application.isbn.IsbnLookupUseCase;
import br.com.yuri.alpha7.application.livro.BookCrudUseCase;
import br.com.yuri.alpha7.domain.autor.model.Autor;
import br.com.yuri.alpha7.domain.editora.model.Editora;
import br.com.yuri.alpha7.domain.editora.repository.EditoraRepository;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import br.com.yuri.alpha7.presentation.livro.view.LivroFormView;

import javax.swing.SwingWorker;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LivroFormPresenter {

    private final LivroFormView     view;
    private final BookCrudUseCase   crudUseCase;
    private final IsbnLookupUseCase isbnLookupUseCase;
    private final EditoraRepository editoraRepository;
    private final Runnable          onSuccess;
    private Long livroId;

    public LivroFormPresenter(LivroFormView view,
                              BookCrudUseCase crudUseCase,
                              IsbnLookupUseCase isbnLookupUseCase,
                              EditoraRepository editoraRepository,
                              Runnable onSuccess) {
        this.view              = view;
        this.crudUseCase       = crudUseCase;
        this.isbnLookupUseCase = isbnLookupUseCase;
        this.editoraRepository = editoraRepository;
        this.onSuccess         = onSuccess;
        view.onIsbnLookup(this::lookupByIsbn);
        view.onSave(this::save);
        view.onCancel(view::close);
    }

    public void initCreate() {
        this.livroId = null;
    }

    public void initEdit(Livro livro) {
        this.livroId = livro.getId();
        view.setLivro(livro);
    }

    private void lookupByIsbn() {
        String isbnStr = view.getIsbn();
        if (isbnStr.isEmpty()) {
            view.showErrorMessage("Digite um ISBN antes de buscar.");
            return;
        }

        ISBN isbn;
        try {
            isbn = new ISBN(isbnStr);
        } catch (Exception e) {
            view.showErrorMessage("ISBN inválido: " + e.getMessage());
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
                    view.showErrorMessage("Nenhum livro encontrado para o ISBN informado.");
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    view.showErrorMessage("Erro ao buscar ISBN: " + cause.getMessage());
                }
            }
        }.execute();
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
        if (view.getTitulo().isEmpty()) {
            view.showErrorMessage("Título é obrigatório.");
            return false;
        }
        if (view.getIsbn().isEmpty()) {
            view.showErrorMessage("ISBN é obrigatório.");
            return false;
        }
        if (view.getAutores().isEmpty()) {
            view.showErrorMessage("Informe ao menos um autor.");
            return false;
        }
        String dateStr = view.getDataPublicacao();
        if (!dateStr.isEmpty() && !dateStr.matches("\\d{4}")) {
            view.showErrorMessage("Publicação inválida. Use apenas o ano (ex: 2003).");
            return false;
        }
        return true;
    }

    private Livro buildLivro() {
        Livro livro = new Livro();

        if (livroId != null) livro.setId(livroId);

        livro.setTitulo(view.getTitulo());
        livro.setIsbn(new ISBN(view.getIsbn()));
        livro.setAutores(parseAutores(view.getAutores()));

        String editoraStr = view.getEditora();
        if (!editoraStr.isEmpty()) {
            Editora editora = editoraRepository.findByNome(editoraStr)
                    .orElseGet(() -> editoraRepository.save(new Editora(editoraStr)));
            livro.setEditora(editora);
        }

        String dateStr = view.getDataPublicacao();
        if (!dateStr.isEmpty()) {
            livro.setDataPublicacao(parsePublishDate(dateStr));
        }


        String idioma = view.getIdioma();
        if (!idioma.isEmpty()) livro.setIdioma(idioma);

        String pagesStr = view.getNumeroPaginas();
        if (!pagesStr.isEmpty()) livro.setNumeroPaginas(Integer.parseInt(pagesStr));

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
