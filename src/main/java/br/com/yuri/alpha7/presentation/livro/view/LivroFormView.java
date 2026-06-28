package br.com.yuri.alpha7.presentation.livro.view;

import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.presentation.View;

/**
 * Contrato do formulário de cadastro e edição de livros no padrão MVP.
 *
 * <p>O {@link br.com.yuri.alpha7.presentation.livro.presenter.LivroFormPresenter} depende
 * exclusivamente desta interface — nunca de {@code LivroFormDialog} diretamente. Isso permite
 * testar todas as regras de validação e o fluxo de busca ISBN com mocks, sem abrir janelas Swing.
 *
 * <p>Os campos de texto ({@code getTitulo}, {@code getIsbn}, etc.) sempre retornam {@code String}
 * — o presenter é responsável por interpretar strings vazias como ausência de valor e realizar
 * validação antes de construir o objeto de domínio.
 */
public interface LivroFormView extends View {

    /**
     * Retorna o título digitado pelo usuário.
     *
     * @return texto do campo título, nunca nulo
     */
    String getTitulo();

    /**
     * Retorna o ISBN digitado pelo usuário, antes de qualquer validação.
     *
     * @return texto do campo ISBN, nunca nulo
     */
    String getIsbn();

    /**
     * Retorna os nomes de autores digitados, separados por vírgula.
     *
     * @return texto do campo autores, nunca nulo
     */
    String getAutores();

    /**
     * Retorna o nome da editora digitado pelo usuário.
     *
     * @return texto do campo editora, nunca nulo
     */
    String getEditora();

    /**
     * Retorna a data de publicação digitada pelo usuário.
     * O presenter aceita apenas o ano no formato {@code yyyy}.
     *
     * @return texto do campo data de publicação, nunca nulo
     */
    String getDataPublicacao();

    /**
     * Retorna o idioma digitado pelo usuário (ex: {@code "en"}, {@code "pt"}).
     *
     * @return texto do campo idioma, nunca nulo
     */
    String getIdioma();

    /**
     * Retorna o número de páginas digitado pelo usuário.
     *
     * @return texto do campo número de páginas, nunca nulo
     */
    String getNumeroPaginas();

    /**
     * Preenche todos os campos do formulário com os dados do livro fornecido.
     * Chamado após uma busca bem-sucedida por ISBN e ao iniciar a edição.
     *
     * @param livro livro cujos dados devem preencher o formulário
     */
    void setLivro(Livro livro);

    /**
     * Registra a ação a ser executada quando o usuário acionar a busca por ISBN.
     *
     * @param acao ação de busca; será executada na EDT
     */
    void onIsbnLookup(Runnable acao);

    /**
     * Registra a ação a ser executada quando o usuário confirmar o salvamento.
     *
     * @param acao ação de salvamento
     */
    void onSave(Runnable acao);

    /**
     * Registra a ação a ser executada quando o usuário cancelar o formulário.
     *
     * @param acao ação de cancelamento (normalmente {@link #close()})
     */
    void onCancel(Runnable acao);

    /**
     * Fecha o formulário (descarta o diálogo ou painel).
     */
    void close();

    /**
     * Habilita ou desabilita o botão de busca por ISBN.
     * Desabilitado durante a consulta assíncrona para evitar requisições duplicadas.
     *
     * @param enabled {@code true} para habilitar, {@code false} para desabilitar
     */
    void setLookupEnabled(boolean enabled);
}

