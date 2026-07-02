package br.com.yuri.alpha7.domain.livro.repository;

/**
 * Objeto de critérios usado na pesquisa avançada de livros.
 *
 * <p>O filtro concentra os campos informados pela tela de listagem para evitar
 * assinaturas longas no repositório e manter a construção da query isolada na
 * infraestrutura. Todos os campos são opcionais; quando nenhum critério possui
 * valor útil, {@link #isEmpty()} retorna {@code true}.</p>
 */
public final class LivroFiltro {

    private final String  termo;
    private final String  autor;
    private final String  editora;
    private final Integer anoMin;
    private final Integer anoMax;
    private final String  idioma;

    /**
     * Cria um filtro de pesquisa de livros.
     *
     * @param termo texto livre pesquisado em campos principais, como título e ISBN
     * @param autor nome ou trecho do nome do autor
     * @param editora nome ou trecho do nome da editora
     * @param anoMin ano mínimo de publicação, inclusive
     * @param anoMax ano máximo de publicação, inclusive
     * @param idioma idioma do livro
     */
    public LivroFiltro(String termo, String autor, String editora,
                       Integer anoMin, Integer anoMax, String idioma) {
        this.termo   = termo;
        this.autor   = autor;
        this.editora = editora;
        this.anoMin  = anoMin;
        this.anoMax  = anoMax;
        this.idioma  = idioma;
    }

    /**
     * Indica se nenhum critério efetivo foi informado.
     *
     * @return {@code true} quando todos os campos textuais estão vazios e não há faixa de ano
     */
    public boolean isEmpty() {
        return !hasText(termo)   && !hasText(autor)
            && !hasText(editora) && anoMin == null
            && anoMax == null    && !hasText(idioma);
    }

    public String  getTermo()   { return termo; }
    public String  getAutor()   { return autor; }
    public String  getEditora() { return editora; }
    public Integer getAnoMin()  { return anoMin; }
    public Integer getAnoMax()  { return anoMax; }
    public String  getIdioma()  { return idioma; }

    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
