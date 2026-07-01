package br.com.yuri.alpha7.application.importacao.parser;

import br.com.yuri.alpha7.application.importacao.model.ImportRecord;
import br.com.yuri.alpha7.domain.exception.ImportException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação de {@link ImportParser} para arquivos CSV.
 *
 * <p>Formato esperado (com cabeçalho):
 * {@code titulo,isbn,autores,editora,dataPublicacao,idioma,numeroPaginas}
 *
 * <p>O campo {@code autores} aceita múltiplos autores separados por vírgula dentro de aspas:
 * {@code "Autor A, Autor B"}.
 */
public class CsvImportParser implements ImportParser {

    @Override
    public String supports() {
        return "csv";
    }

    @Override
    public List<ImportRecord> parse(InputStream stream) {
        try {
            Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .build()
                    .parse(reader);

            List<ImportRecord> result = new ArrayList<>();
            for (CSVRecord record : records) {
                result.add(new ImportRecord(
                        record.get("titulo"),
                        record.get("isbn"),
                        record.get("autores"),
                        record.get("editora"),
                        record.get("dataPublicacao"),
                        record.get("idioma"),
                        record.get("numeroPaginas")
                ));
            }
            return result;
        } catch (Exception e) {
            throw new ImportException("Erro ao ler arquivo CSV: " + e.getMessage());
        }
    }
}
