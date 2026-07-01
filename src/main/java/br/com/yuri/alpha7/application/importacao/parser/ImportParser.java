package br.com.yuri.alpha7.application.importacao.parser;

import br.com.yuri.alpha7.application.importacao.model.ImportRecord;
import br.com.yuri.alpha7.application.importacao.ImportUseCase;

import java.io.InputStream;
import java.util.List;

/**
 * Strategy de importação de livros.
 *
 * <p>Cada implementação é responsável por converter um {@link InputStream} de um formato
 * específico (CSV, XML, etc.) em uma lista de {@link ImportRecord}s que o
 * {@link ImportUseCase} processará de forma uniforme.
 */
public interface ImportParser {

    /**
     * Extensão de arquivo suportada por esta estratégia, em letras minúsculas (ex: {@code "csv"}).
     */
    String supports();

    /**
     * Lê o stream e retorna os registros encontrados.
     *
     * @param stream stream do arquivo de importação
     * @return lista de registros extraídos
     */
    List<ImportRecord> parse(InputStream stream);
}
