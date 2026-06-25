package br.com.yuri.alpha7.application.importacao;

import br.com.yuri.alpha7.application.UnitOfWork;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportUseCaseTest {

    @Mock private UnitOfWork unitOfWork;
    @Mock private LivroRepository livroRepository;
    @Mock private AutorRepository autorRepository;
    @Mock private EditoraRepository editoraRepository;

    @InjectMocks
    private ImportUseCase useCase;

    @BeforeEach
    void setupUnitOfWork() {
        doAnswer(invocation -> {
            Runnable action = invocation.getArgument(0);
            action.run();
            return null;
        }).when(unitOfWork).execute(any(Runnable.class));
    }

    @Test
    @DisplayName(
            "Given a valid CSV line with a new book," +
            " when importCsv is called," +
            " then book is saved and result counts one success"
    )
    void shouldSaveNewBookWhenCsvLineIsValid() {
        when(editoraRepository.findByNome("Prentice Hall")).thenReturn(Optional.empty());
        when(editoraRepository.save(any())).thenReturn(new Editora("Prentice Hall"));
        when(autorRepository.findByNome("Robert Martin")).thenReturn(Optional.empty());
        when(autorRepository.save(any())).thenReturn(new Autor("Robert Martin"));
        when(livroRepository.findByIsbn(any())).thenReturn(Optional.empty());
        when(livroRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ImportResult result = useCase.importCsv(csvStream(
                "titulo,isbn,autores,editora,dataPublicacao,idioma,numeroPaginas\n" +
                "Clean Code,9780132350884,Robert Martin,Prentice Hall,2008-08-01,English,431"
        ));

        assertEquals(1, result.getTotalSaved());
        assertFalse(result.hasErrors());
        verify(livroRepository).save(any(Livro.class));
    }

    @Test
    @DisplayName(
            "Given a CSV line with an ISBN already registered," +
            " when importCsv is called," +
            " then existing book title is updated"
    )
    void shouldUpdateTitleWhenBookAlreadyExists() {
        Livro existingBook = new Livro();
        existingBook.setIsbn(new ISBN("9780132350884"));
        existingBook.setTitulo("Clean Code");

        when(editoraRepository.findByNome(any())).thenReturn(Optional.of(new Editora("Prentice Hall")));
        when(autorRepository.findByNome(any())).thenReturn(Optional.of(new Autor("Robert Martin")));
        when(livroRepository.findByIsbn(any())).thenReturn(Optional.of(existingBook));
        when(livroRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ImportResult result = useCase.importCsv(csvStream(
                "titulo,isbn,autores,editora,dataPublicacao,idioma,numeroPaginas\n" +
                "Clean Code 2nd Ed,9780132350884,Robert Martin,Prentice Hall,2022-01-01,English,500"
        ));

        assertEquals(1, result.getTotalSaved());
        verify(livroRepository).save(argThat(l -> "Clean Code 2nd Ed".equals(l.getTitulo())));
    }

    @Test
    @DisplayName(
            "Given a CSV line with an invalid ISBN," +
            " when importCsv is called," +
            " then error is recorded and processing continues"
    )
    void shouldAddErrorAndContinueWhenIsbnIsInvalid() {
        ImportResult result = useCase.importCsv(csvStream(
                "titulo,isbn,autores,editora,dataPublicacao,idioma,numeroPaginas\n" +
                "Invalid Book,INVALID,Author,Publisher,2023-01-01,EN,100"
        ));

        assertEquals(0, result.getTotalSaved());
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        verify(livroRepository, never()).save(any());
    }

    @Test
    @DisplayName(
            "Given a CSV line with an already registered publisher," +
            " when importCsv is called," +
            " then existing publisher is reused"
    )
    void shouldReuseExistingPublisherWhenAlreadyRegistered() {
        Editora existingPublisher = new Editora("O'Reilly");
        when(editoraRepository.findByNome("O'Reilly")).thenReturn(Optional.of(existingPublisher));
        when(autorRepository.findByNome(any())).thenReturn(Optional.of(new Autor("Joshua Bloch")));
        when(livroRepository.findByIsbn(any())).thenReturn(Optional.empty());
        when(livroRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        useCase.importCsv(csvStream(
                "titulo,isbn,autores,editora,dataPublicacao,idioma,numeroPaginas\n" +
                "Effective Java,9780134685991,Joshua Bloch,O'Reilly,2018-01-06,English,412"
        ));

        verify(editoraRepository, never()).save(any());
        verify(livroRepository).save(argThat(l -> existingPublisher.equals(l.getEditora())));
    }

    @Test
    @DisplayName(
            "Given a CSV with one valid and one invalid line," +
            " when importCsv is called," +
            " then one success and one error are counted"
    )
    void shouldCountSuccessAndErrorsWhenProcessingMultipleLines() {
        when(editoraRepository.findByNome(any())).thenReturn(Optional.of(new Editora("Prentice Hall")));
        when(autorRepository.findByNome(any())).thenReturn(Optional.of(new Autor("Robert Martin")));
        when(livroRepository.findByIsbn(any())).thenReturn(Optional.empty());
        when(livroRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ImportResult result = useCase.importCsv(csvStream(
                "titulo,isbn,autores,editora,dataPublicacao,idioma,numeroPaginas\n" +
                "Clean Code,9780132350884,Robert Martin,Prentice Hall,2008-08-01,English,431\n" +
                "Invalid,INVALID,Author,Publisher,2023-01-01,EN,100"
        ));

        assertEquals(2, result.getTotalProcessed());
        assertEquals(1, result.getTotalSaved());
        assertEquals(1, result.getErrors().size());
    }

    @Test
    @DisplayName(
            "Given a CSV line with empty optional fields," +
            " when importCsv is called," +
            " then optional fields are not set on the saved book"
    )
    void shouldNotSetOptionalFieldsWhenTheyAreEmpty() {
        when(editoraRepository.findByNome(any())).thenReturn(Optional.of(new Editora("Pub")));
        when(autorRepository.findByNome(any())).thenReturn(Optional.of(new Autor("Author")));
        when(livroRepository.findByIsbn(any())).thenReturn(Optional.empty());
        when(livroRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ImportResult result = useCase.importCsv(csvStream(
                "titulo,isbn,autores,editora,dataPublicacao,idioma,numeroPaginas\n" +
                "Book Title,9780132350884,Author,Pub,,,\n"
        ));

        assertEquals(1, result.getTotalSaved());
        verify(livroRepository).save(argThat(l ->
                l.getDataPublicacao() == null &&
                l.getIdioma() == null &&
                l.getNumeroPaginas() == null
        ));
    }

    @Test
    @DisplayName(
            "Given a CSV line with an author field containing a trailing comma," +
            " when importCsv is called," +
            " then the empty entry is skipped and only valid authors are assigned"
    )
    void shouldSkipEmptyAuthorNamesInCsvLine() {
        when(editoraRepository.findByNome(any())).thenReturn(Optional.of(new Editora("Pub")));
        when(autorRepository.findByNome("Author A")).thenReturn(Optional.of(new Autor("Author A")));
        when(livroRepository.findByIsbn(any())).thenReturn(Optional.empty());
        when(livroRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // "Author A," contains a trailing comma → split produces ["Author A", ""] → empty entry is skipped
        ImportResult result = useCase.importCsv(csvStream(
                "titulo,isbn,autores,editora,dataPublicacao,idioma,numeroPaginas\n" +
                "Book,9780132350884,\"Author A,\",Pub,2023-01-01,EN,100\n"
        ));

        assertEquals(1, result.getTotalSaved());
        verify(livroRepository).save(argThat(l -> l.getAutores().size() == 1));
    }

    private InputStream csvStream(String content) {
        return new ByteArrayInputStream(content.getBytes());
    }
}
