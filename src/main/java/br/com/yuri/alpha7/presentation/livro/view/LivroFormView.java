package br.com.yuri.alpha7.presentation.livro.view;

import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.presentation.View;

public interface LivroFormView extends View {
        String getTitulo();
        String getIsbn();
        String getAutores();
        String getEditora();
        String getDataPublicacao();
        String getIdioma();
        String getNumeroPaginas();
        void setLivro(Livro livro);
        void onIsbnLookup(Runnable acao);
        void onSave(Runnable acao);
        void onCancel(Runnable acao);
        void close();
        void setLookupEnabled(boolean enabled);
}

