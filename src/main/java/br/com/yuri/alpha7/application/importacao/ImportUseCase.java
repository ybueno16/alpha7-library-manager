package br.com.yuri.alpha7.application.importacao;

import br.com.yuri.alpha7.application.UnitOfWork;
import br.com.yuri.alpha7.domain.autor.model.Autor;
import br.com.yuri.alpha7.domain.autor.repository.AutorRepository;
import br.com.yuri.alpha7.domain.editora.model.Editora;
import br.com.yuri.alpha7.domain.editora.repository.EditoraRepository;
import br.com.yuri.alpha7.domain.exception.ImportException;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.repository.LivroRepository;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ImportUseCase {

    private final UnitOfWork unitOfWork;
    private final LivroRepository livroRepository;
    private final AutorRepository autorRepository;
    private final EditoraRepository editoraRepository;

    public ImportUseCase(
            UnitOfWork unitOfWork,
            LivroRepository livroRepository,
            AutorRepository autorRepository,
            EditoraRepository editoraRepository
    ) {
        this.unitOfWork = unitOfWork;
        this.livroRepository = livroRepository;
        this.autorRepository = autorRepository;
        this.editoraRepository = editoraRepository;
    }

    public ImportResult importCsv(InputStream csv) {
        ImportResult result = new ImportResult();
        Iterable<CSVRecord> records = readCsv(csv);
        int lineNumber = 2;

        for (CSVRecord record : records) {
            final int line = lineNumber++;
            try {
                unitOfWork.execute(() -> processLine(record));
                result.incrementSuccess();
            } catch (Exception e) {
                result.addError("Line " + line + ": " + e.getMessage());
            }
        }
        return result;
    }

    private void processLine(CSVRecord record) {
        String isbnValue      = record.get("isbn").trim();
        String titulo         = record.get("titulo").trim();
        String publisherName  = record.get("editora").trim();
        String authorNames    = record.get("autores").trim();
        String publicationDate = record.get("dataPublicacao").trim();
        String language       = record.get("idioma").trim();
        String pageCount      = record.get("numeroPaginas").trim();

        ISBN isbn = new ISBN(isbnValue);

        Editora editora = editoraRepository.findByNome(publisherName)
                .orElseGet(() -> editoraRepository.save(new Editora(publisherName)));

        List<Autor> authors = resolveAuthors(authorNames);

        Optional<Livro> existingBook = livroRepository.findByIsbn(isbn);

        if (existingBook.isPresent()) {
            Livro livro = existingBook.get();
            livro.setTitulo(titulo);
            livro.setEditora(editora);
            livro.setAutores(authors);
            assignOptionalFields(livro, publicationDate, language, pageCount);
            livroRepository.save(livro);
        } else {
            Livro livro = new Livro();
            livro.setIsbn(isbn);
            livro.setTitulo(titulo);
            livro.setEditora(editora);
            livro.setAutores(authors);
            assignOptionalFields(livro, publicationDate, language, pageCount);
            livroRepository.save(livro);
        }
    }

    private List<Autor> resolveAuthors(String authorNames) {
        List<Autor> authors = new ArrayList<>();
        for (String name : authorNames.split(",")) {
            String trimmedName = name.trim();
            if (trimmedName.isEmpty()) continue;
            Autor autor = autorRepository.findByNome(trimmedName)
                    .orElseGet(() -> autorRepository.save(new Autor(trimmedName)));
            authors.add(autor);
        }
        return authors;
    }

    private void assignOptionalFields(Livro livro, String publicationDate, String language, String pageCount) {
        if (!publicationDate.isEmpty()) {
            livro.setDataPublicacao(LocalDate.parse(publicationDate));
        }
        if (!language.isEmpty()) {
            livro.setIdioma(language);
        }
        if (!pageCount.isEmpty()) {
            livro.setNumeroPaginas(Integer.parseInt(pageCount));
        }
    }

    private Iterable<CSVRecord> readCsv(InputStream csv) {
        try {
            Reader reader = new InputStreamReader(csv);
            return CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .build()
                    .parse(reader);
        } catch (Exception e) {
            throw new ImportException("Error reading CSV file: " + e.getMessage());
        }
    }
}
