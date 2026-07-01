package br.com.yuri.alpha7.application.importacao;

import br.com.yuri.alpha7.application.UnitOfWork;
import br.com.yuri.alpha7.application.importacao.model.ImportPreviewRecord;
import br.com.yuri.alpha7.application.importacao.model.ImportResult;
import br.com.yuri.alpha7.application.importacao.parser.CsvImportParser;
import br.com.yuri.alpha7.domain.autor.model.Autor;
import br.com.yuri.alpha7.domain.autor.repository.AutorRepository;
import br.com.yuri.alpha7.domain.editora.model.Editora;
import br.com.yuri.alpha7.domain.editora.repository.EditoraRepository;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.repository.LivroRepository;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportUseCaseTest {

    @Mock private UnitOfWork        unitOfWork;
    @Mock private LivroRepository   livroRepository;
    @Mock private AutorRepository   autorRepository;
    @Mock private EditoraRepository editoraRepository;

    private ImportUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ImportUseCase(
                unitOfWork, livroRepository, autorRepository, editoraRepository,
                Arrays.asList(new CsvImportParser())
        );
        lenient().doAnswer(invocation -> {
            Runnable action = invocation.getArgument(0);
            action.run();
            return null;
        }).when(unitOfWork).execute(any(Runnable.class));
    }

    @Test
    @DisplayName(
            "Given a valid CSV line with a new book," +
            " when importFile is called," +
            " then book is saved and result counts one new import"
    )
    void shouldSaveNewBookWhenCsvLineIsValid() {
        when(livroRepository.findByIsbn(any())).thenReturn(Optional.empty());
        when(livroRepository.findByIsbnIncludingDeleted(any())).thenReturn(Optional.empty());
        when(editoraRepository.findByNome("Prentice Hall")).thenReturn(Optional.empty());
        when(editoraRepository.save(any())).thenReturn(new Editora("Prentice Hall"));
        when(autorRepository.findByNome("Robert Martin")).thenReturn(Optional.empty());
        when(autorRepository.save(any())).thenReturn(new Autor("Robert Martin"));
        when(livroRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ImportResult result = importFile(csvStream(
                "titulo,isbn,autores,editora,dataPublicacao,idioma,numeroPaginas\n" +
                "Clean Code,9780132350884,Robert Martin,Prentice Hall,2008-08-01,English,431"
        ), "test.csv");

        assertEquals(1, result.getTotalNew());
        assertEquals(0, result.getTotalSkipped());
        assertFalse(result.hasErrors());
        verify(livroRepository).save(any(Livro.class));
    }

    @Test
    @DisplayName(
            "Given a CSV line with an ISBN already active in the collection," +
            " when importFile is called," +
            " then book is skipped and counted as skipped"
    )
    void shouldSkipWhenBookAlreadyExistsInCollection() {
        Livro existingBook = new Livro();
        existingBook.setIsbn(new ISBN("9780132350884"));
        existingBook.setTitulo("Clean Code");

        when(livroRepository.findByIsbn(any())).thenReturn(Optional.of(existingBook));

        ImportResult result = importFile(csvStream(
                "titulo,isbn,autores,editora,dataPublicacao,idioma,numeroPaginas\n" +
                "Clean Code,9780132350884,Robert Martin,Prentice Hall,2022-01-01,English,500"
        ), "test.csv");

        assertEquals(0, result.getTotalNew());
        assertEquals(1, result.getTotalSkipped());
        assertFalse(result.hasErrors());
        verify(livroRepository, never()).save(any());
    }

    @Test
    @DisplayName(
            "Given a CSV line with an ISBN of a soft-deleted book," +
            " when importFile is called," +
            " then book is reactivated and counted as new"
    )
    void shouldReactivateSoftDeletedBookWhenIsbnMatchesDeletedRecord() {
        Livro deletedBook = new Livro();
        deletedBook.setIsbn(new ISBN("9780132350884"));
        deletedBook.setTitulo("Clean Code");

        when(livroRepository.findByIsbn(any())).thenReturn(Optional.empty());
        when(livroRepository.findByIsbnIncludingDeleted(any())).thenReturn(Optional.of(deletedBook));
        when(editoraRepository.findByNome(any())).thenReturn(Optional.of(new Editora("Prentice Hall")));
        when(autorRepository.findByNome(any())).thenReturn(Optional.of(new Autor("Robert Martin")));
        when(livroRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ImportResult result = importFile(csvStream(
                "titulo,isbn,autores,editora,dataPublicacao,idioma,numeroPaginas\n" +
                "Clean Code,9780132350884,Robert Martin,Prentice Hall,2008-08-01,English,431"
        ), "test.csv");

        assertEquals(1, result.getTotalNew());
        assertEquals(0, result.getTotalSkipped());
        assertFalse(result.hasErrors());
        verify(livroRepository).save(any(Livro.class));
    }

    @Test
    @DisplayName(
            "Given a CSV line with an invalid ISBN," +
            " when importFile is called," +
            " then error is recorded and processing continues"
    )
    void shouldAddErrorAndContinueWhenIsbnIsInvalid() {
        ImportResult result = importFile(csvStream(
                "titulo,isbn,autores,editora,dataPublicacao,idioma,numeroPaginas\n" +
                "Invalid Book,INVALID,Author,Publisher,2023-01-01,EN,100"
        ), "test.csv");

        assertEquals(0, result.getTotalNew());
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        verify(livroRepository, never()).save(any());
    }

    @Test
    @DisplayName(
            "Given a CSV line with an already registered publisher," +
            " when importFile is called," +
            " then existing publisher is reused"
    )
    void shouldReuseExistingPublisherWhenAlreadyRegistered() {
        Editora existingPublisher = new Editora("O'Reilly");
        when(livroRepository.findByIsbn(any())).thenReturn(Optional.empty());
        when(livroRepository.findByIsbnIncludingDeleted(any())).thenReturn(Optional.empty());
        when(editoraRepository.findByNome("O'Reilly")).thenReturn(Optional.of(existingPublisher));
        when(autorRepository.findByNome(any())).thenReturn(Optional.of(new Autor("Joshua Bloch")));
        when(livroRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        importFile(csvStream(
                "titulo,isbn,autores,editora,dataPublicacao,idioma,numeroPaginas\n" +
                "Effective Java,9780134685991,Joshua Bloch,O'Reilly,2018-01-06,English,412"
        ), "test.csv");

        verify(editoraRepository, never()).save(any());
        verify(livroRepository).save(argThat(l -> existingPublisher.equals(l.getEditora())));
    }

    @Test
    @DisplayName(
            "Given a CSV with one valid and one invalid line," +
            " when importFile is called," +
            " then one success and one error are counted"
    )
    void shouldCountSuccessAndErrorsWhenProcessingMultipleLines() {
        when(livroRepository.findByIsbn(any())).thenReturn(Optional.empty());
        when(livroRepository.findByIsbnIncludingDeleted(any())).thenReturn(Optional.empty());
        when(editoraRepository.findByNome(any())).thenReturn(Optional.of(new Editora("Prentice Hall")));
        when(autorRepository.findByNome(any())).thenReturn(Optional.of(new Autor("Robert Martin")));
        when(livroRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ImportResult result = importFile(csvStream(
                "titulo,isbn,autores,editora,dataPublicacao,idioma,numeroPaginas\n" +
                "Clean Code,9780132350884,Robert Martin,Prentice Hall,2008-08-01,English,431\n" +
                "Invalid,INVALID,Author,Publisher,2023-01-01,EN,100"
        ), "test.csv");

        assertEquals(2, result.getTotalProcessed());
        assertEquals(1, result.getTotalNew());
        assertEquals(1, result.getErrors().size());
    }

    @Test
    @DisplayName(
            "Given a CSV line with empty optional fields," +
            " when importFile is called," +
            " then optional fields are not set on the saved book"
    )
    void shouldNotSetOptionalFieldsWhenTheyAreEmpty() {
        when(livroRepository.findByIsbn(any())).thenReturn(Optional.empty());
        when(livroRepository.findByIsbnIncludingDeleted(any())).thenReturn(Optional.empty());
        when(editoraRepository.findByNome(any())).thenReturn(Optional.of(new Editora("Pub")));
        when(autorRepository.findByNome(any())).thenReturn(Optional.of(new Autor("Author")));
        when(livroRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ImportResult result = importFile(csvStream(
                "titulo,isbn,autores,editora,dataPublicacao,idioma,numeroPaginas\n" +
                "Book Title,9780132350884,Author,Pub,,,\n"
        ), "test.csv");

        assertEquals(1, result.getTotalNew());
        verify(livroRepository).save(argThat(l ->
                l.getDataPublicacao() == null &&
                l.getIdioma() == null &&
                l.getNumeroPaginas() == null
        ));
    }

    @Test
    @DisplayName(
            "Given a CSV line with an author field containing a trailing comma," +
            " when importFile is called," +
            " then the empty entry is skipped and only valid authors are assigned"
    )
    void shouldSkipEmptyAuthorNamesInCsvLine() {
        when(livroRepository.findByIsbn(any())).thenReturn(Optional.empty());
        when(livroRepository.findByIsbnIncludingDeleted(any())).thenReturn(Optional.empty());
        when(editoraRepository.findByNome(any())).thenReturn(Optional.of(new Editora("Pub")));
        when(autorRepository.findByNome("Author A")).thenReturn(Optional.of(new Autor("Author A")));
        when(livroRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ImportResult result = importFile(csvStream(
                "titulo,isbn,autores,editora,dataPublicacao,idioma,numeroPaginas\n" +
                "Book,9780132350884,\"Author A,\",Pub,2023-01-01,EN,100\n"
        ), "test.csv");

        assertEquals(1, result.getTotalNew());
        verify(livroRepository).save(argThat(l -> l.getAutores().size() == 1));
    }

    @Test
    @DisplayName(
            "Given a file with an unsupported extension," +
            " when preview is called," +
            " then ImportException is thrown"
    )
    void shouldThrowImportExceptionWhenFileExtensionIsUnsupported() {
        assertThrows(br.com.yuri.alpha7.domain.exception.ImportException.class, () ->
                useCase.preview(csvStream("anything"), "data.xlsx")
        );
    }

    @Test
    @DisplayName(
            "Given a CSV line with an invalid date format," +
            " when preview is called," +
            " then the record is flagged with ERRO status"
    )
    void shouldFlagErrorWhenDateFormatIsInvalid() {
        List<ImportPreviewRecord> previews = useCase.preview(csvStream(
                "titulo,isbn,autores,editora,dataPublicacao,idioma,numeroPaginas\n" +
                "Book,9780132350884,Author,Pub,31/12/2023,EN,100"
        ), "test.csv");

        assertEquals(1, previews.size());
        assertEquals(ImportPreviewRecord.Status.ERRO, previews.get(0).getStatus());
    }

    @Test
    @DisplayName(
            "Given a CSV line with a non-numeric page count," +
            " when preview is called," +
            " then the record is flagged with ERRO status"
    )
    void shouldFlagErrorWhenPageCountIsNotNumeric() {
        List<ImportPreviewRecord> previews = useCase.preview(csvStream(
                "titulo,isbn,autores,editora,dataPublicacao,idioma,numeroPaginas\n" +
                "Book,9780132350884,Author,Pub,2023-01-01,EN,abc"
        ), "test.csv");

        assertEquals(1, previews.size());
        assertEquals(ImportPreviewRecord.Status.ERRO, previews.get(0).getStatus());
    }

    @Test
    @DisplayName(
            "Given a preview with a record deselected by the user," +
            " when importSelected is called," +
            " then the deselected record is skipped and not saved"
    )
    void shouldSkipDeselectedRecordsOnImport() {
        List<ImportPreviewRecord> previews = useCase.preview(csvStream(
                "titulo,isbn,autores,editora,dataPublicacao,idioma,numeroPaginas\n" +
                "Book,9780132350884,Author,Pub,2023-01-01,EN,100"
        ), "test.csv");

        previews.get(0).setSelecionado(false);
        ImportResult result = useCase.importSelected(previews);

        assertEquals(0, result.getTotalNew());
        verify(livroRepository, never()).save(any());
    }

    @Test
    @DisplayName(
            "Given a CSV line with an empty titulo field," +
            " when preview is called," +
            " then the record is flagged with ERRO status"
    )
    void shouldFlagErrorWhenTituloIsEmpty() {
        List<ImportPreviewRecord> previews = useCase.preview(csvStream(
                "titulo,isbn,autores,editora,dataPublicacao,idioma,numeroPaginas\n" +
                ",9780132350884,Author,Pub,2023-01-01,EN,100"
        ), "test.csv");

        assertEquals(1, previews.size());
        assertEquals(ImportPreviewRecord.Status.ERRO, previews.get(0).getStatus());
    }

    @Test
    @DisplayName(
            "Given a filename without a dot," +
            " when preview is called," +
            " then ImportException is thrown"
    )
    void shouldThrowImportExceptionWhenFilenameHasNoExtension() {
        assertThrows(br.com.yuri.alpha7.domain.exception.ImportException.class, () ->
                useCase.preview(csvStream("anything"), "nodotfile")
        );
    }

    @Test
    @DisplayName(
            "Given a valid NOVO record and the unit of work throws IsbnInvalidoException," +
            " when importSelected is called," +
            " then the error is recorded in the result"
    )
    void shouldAddErrorWhenSaveThrowsIsbnInvalidoException() {
        doThrow(new br.com.yuri.alpha7.domain.exception.IsbnInvalidoException("dup"))
                .when(unitOfWork).execute(any(Runnable.class));

        List<ImportPreviewRecord> previews = useCase.preview(csvStream(
                "titulo,isbn,autores,editora,dataPublicacao,idioma,numeroPaginas\n" +
                "Book,9780132350884,Author,Pub,2023-01-01,EN,100"
        ), "test.csv");
        ImportResult result = useCase.importSelected(previews);

        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
    }

    @Test
    @DisplayName(
            "Given a valid NOVO record and the unit of work throws ImportException," +
            " when importSelected is called," +
            " then the error is recorded in the result"
    )
    void shouldAddErrorWhenSaveThrowsImportException() {
        doThrow(new br.com.yuri.alpha7.domain.exception.ImportException("parse fail"))
                .when(unitOfWork).execute(any(Runnable.class));

        List<ImportPreviewRecord> previews = useCase.preview(csvStream(
                "titulo,isbn,autores,editora,dataPublicacao,idioma,numeroPaginas\n" +
                "Book,9780132350884,Author,Pub,2023-01-01,EN,100"
        ), "test.csv");
        ImportResult result = useCase.importSelected(previews);

        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
    }

    @Test
    @DisplayName(
            "Given a valid NOVO record and the unit of work throws a generic exception," +
            " when importSelected is called," +
            " then the error is recorded in the result"
    )
    void shouldAddErrorWhenSaveThrowsGenericException() {
        doThrow(new RuntimeException("db failure"))
                .when(unitOfWork).execute(any(Runnable.class));

        List<ImportPreviewRecord> previews = useCase.preview(csvStream(
                "titulo,isbn,autores,editora,dataPublicacao,idioma,numeroPaginas\n" +
                "Book,9780132350884,Author,Pub,2023-01-01,EN,100"
        ), "test.csv");
        ImportResult result = useCase.importSelected(previews);

        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
    }

    private ImportResult importFile(InputStream stream, String filename) {
        List<ImportPreviewRecord> previews = useCase.preview(stream, filename);
        return useCase.importSelected(previews);
    }

    private InputStream csvStream(String content) {
        return new ByteArrayInputStream(content.getBytes());
    }
}
