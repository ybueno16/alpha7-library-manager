package br.com.yuri.alpha7.application.importacao.parser;

import br.com.yuri.alpha7.application.importacao.model.ImportRecord;
import br.com.yuri.alpha7.domain.exception.ImportException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação de {@link ImportParser} para arquivos CSV.
 *
 * <p>Formato esperado (com cabeçalho):
 * {@code titulo,isbn,autores,editora,dataPublicacao,idioma,numeroPaginas}
 *
 * <p>O campo {@code autores} aceita múltiplos autores separados por ponto-e-vírgula:
 * {@code "Autor A; Autor B"}.
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
            PushbackReader pr = new PushbackReader(reader, 1);
            int first = pr.read();
            if (first == -1) {
                return new ArrayList<>();
            }
            if (first != '\uFEFF') {
                pr.unread(first);
            }

            List<ImportRecord> result = new ArrayList<>();
            try (CSVParser csvParser = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .build()
                    .parse(pr)) {
                for (CSVRecord record : csvParser) {
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
            }
            return result;
        } catch (Exception e) {
            throw new ImportException("Erro ao ler arquivo CSV: " + e.getMessage());
        }
    }
}
