package br.com.yuri.alpha7.presentation.livro.view;

import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.presentation.CrudView;

import java.util.List;
import java.util.Optional;



/**
 * Contrato da tela de listagem de livros no padrão MVP.
 *
 * <p>O {@link br.com.yuri.alpha7.presentation.livro.presenter.LivroListPresenter} depende
 * exclusivamente desta interface — nunca de {@code LivroListPanel} diretamente. Isso permite
 * testar o presenter com um mock sem instanciar componentes Swing.
 *
 * <p>Os métodos {@code on*} registram callbacks: o presenter passa lambdas que serão invocados
 * quando o usuário interagir com os respectivos controles da view (botões, campo de busca).
 */
public interface LivroListView extends CrudView {

    /**
     * Exibe a lista de livros na tabela.
     *
     * @param livros lista de livros a exibir; pode ser vazia, mas nunca nula
     */
    void showLivros(List<Livro> livros);

    /**
     * Retorna o texto atual do campo de busca.
     *
     * @return texto digitado pelo usuário, nunca nulo
     */
    String getSearchTerm();

    /**
     * Retorna o livro selecionado na tabela (seleção única, usado pelo fluxo de edição).
     *
     * @return {@link Optional} com o livro selecionado, ou vazio se nenhum estiver selecionado
     */
    Optional<Livro> getSelectedLivro();

    /**
     * Retorna todos os livros selecionados na tabela (seleção múltipla, usado pelo fluxo de exclusão em lote).
     *
     * @return lista de livros selecionados; vazia se nenhum estiver selecionado
     */
    List<Livro> getSelectedLivros();

    /**
     * Exibe um diálogo de confirmação e retorna a decisão do usuário.
     *
     * @param message mensagem a exibir no diálogo
     * @return {@code true} se o usuário confirmou, {@code false} se cancelou
     */
    boolean confirm(String message);

    /**
     * Registra a ação a ser executada quando o usuário solicitar importação de CSV.
     *
     * @param acao ação de importação
     */
    void onImport(Runnable acao);

    /**
     * Registra a ação a ser executada quando o usuário solicitar exportação do acervo para CSV.
     *
     * @param acao ação de exportação
     */
    void onExport(Runnable acao);

    void onSearch(Runnable acao);

    void showPaginationInfo(int currentPage, int totalPages);

    void onNextPage(Runnable acao);

    void onPreviousPage(Runnable acao);

    String getAutorFiltro();

    String getEditoraFiltro();

    String getAnoDe();

    String getAnoAte();

    String getIdiomaFiltro();

    void setEditoraOptions(List<String> editoras);

    void setIdiomaOptions(List<String> idiomas);
}
