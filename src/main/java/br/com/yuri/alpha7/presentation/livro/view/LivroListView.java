package br.com.yuri.alpha7.presentation.livro.view;

import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.presentation.CrudView;

import java.util.List;
import java.util.Optional;

public interface LivroListView extends CrudView {
    void showLivros(List<Livro> livros);
    String getSearchTerm();
    Optional<Livro> getSelectedLivro();
    void onImport(Runnable acao);
    void onSearch(Runnable acao);
}
