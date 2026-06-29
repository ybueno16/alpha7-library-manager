package br.com.yuri.alpha7.presentation.livro.presenter;

import br.com.yuri.alpha7.application.editora.EditoraUseCase;
import br.com.yuri.alpha7.application.isbn.IsbnLookupUseCase;
import br.com.yuri.alpha7.application.livro.BookCrudUseCase;
import br.com.yuri.alpha7.application.livro.BookSearchUseCase;
import br.com.yuri.alpha7.domain.editora.model.Editora;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import br.com.yuri.alpha7.presentation.livro.view.LivroFormView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LivroFormPresenterTest {

    @Mock private LivroFormView     view;
    @Mock private BookCrudUseCase   crudUseCase;
    @Mock private BookSearchUseCase searchUseCase;
    @Mock private IsbnLookupUseCase isbnLookupUseCase;
    @Mock private EditoraUseCase    editoraUseCase;

    private Runnable onSuccess;
    private Runnable isbnLookupAction;
    private Runnable saveAction;

    private static final String VALID_ISBN = "9780132350884";

    @BeforeEach
    void setUp() {
        onSuccess = mock(Runnable.class);
        new LivroFormPresenter(view, crudUseCase, searchUseCase, isbnLookupUseCase, editoraUseCase, onSuccess);

        ArgumentCaptor<Runnable> isbnCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(view).onIsbnLookup(isbnCaptor.capture());
        isbnLookupAction = isbnCaptor.getValue();

        ArgumentCaptor<Runnable> saveCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(view).onSave(saveCaptor.capture());
        saveAction = saveCaptor.getValue();
    }

    @Test
    @DisplayName(
            "Given a livro to edit," +
            " when initEdit is called," +
            " then view is populated with the livro data"
    )
    void shouldPopulateViewWhenInitEdit() {
        Livro livro = livroWithId(1L);
        LivroFormPresenter presenter = new LivroFormPresenter(
                view, crudUseCase, searchUseCase, isbnLookupUseCase, editoraUseCase, onSuccess);

        presenter.initEdit(livro);

        verify(view, atLeastOnce()).setLivro(livro);
    }

    @Test
    @DisplayName(
            "Given an empty ISBN field," +
            " when ISBN lookup is triggered," +
            " then an error message is shown"
    )
    void shouldShowErrorWhenIsbnIsEmptyOnLookup() {
        when(view.getIsbn()).thenReturn("");

        isbnLookupAction.run();

        verify(view).showValidationError(anyString());
        verify(isbnLookupUseCase, never()).findByIsbn(any());
    }

    @Test
    @DisplayName(
            "Given an invalid ISBN format," +
            " when ISBN lookup is triggered," +
            " then an error message is shown"
    )
    void shouldShowErrorWhenIsbnIsInvalidOnLookup() {
        when(view.getIsbn()).thenReturn("123");

        isbnLookupAction.run();

        verify(view).showValidationError(anyString());
        verify(isbnLookupUseCase, never()).findByIsbn(any());
    }

    @Test
    @DisplayName(
            "Given a valid ISBN and book found in OpenLibrary," +
            " when ISBN lookup is triggered," +
            " then form is filled and lookup button is re-enabled"
    )
    void shouldFillFormWhenIsbnFound() throws Exception {
        Livro livro = new Livro();
        when(view.getIsbn()).thenReturn(VALID_ISBN);
        when(isbnLookupUseCase.findByIsbn(any())).thenReturn(Optional.of(livro));

        isbnLookupAction.run();
        Thread.sleep(200);
        SwingUtilities.invokeAndWait(() -> {});

        verify(view).setLookupEnabled(false);
        verify(view).setLivro(livro);
        verify(view).setLookupEnabled(true);
    }

    @Test
    @DisplayName(
            "Given a valid ISBN and book not found in OpenLibrary," +
            " when ISBN lookup is triggered," +
            " then an error message is shown and lookup button is re-enabled"
    )
    void shouldShowErrorWhenIsbnNotFound() throws Exception {
        when(view.getIsbn()).thenReturn(VALID_ISBN);
        when(isbnLookupUseCase.findByIsbn(any())).thenReturn(Optional.empty());

        isbnLookupAction.run();
        Thread.sleep(200);
        SwingUtilities.invokeAndWait(() -> {});

        verify(view).setLookupEnabled(true);
        verify(view).showValidationError(anyString());
        verify(view, never()).setLivro(any());
    }

    @Test
    @DisplayName(
            "Given a valid ISBN and OpenLibrary unavailable," +
            " when ISBN lookup is triggered," +
            " then an error message is shown and lookup button is re-enabled"
    )
    void shouldShowErrorWhenIsbnLookupThrows() throws Exception {
        when(view.getIsbn()).thenReturn(VALID_ISBN);
        when(isbnLookupUseCase.findByIsbn(any())).thenThrow(new RuntimeException("timeout"));

        isbnLookupAction.run();

        verify(view, timeout(3000)).setLookupEnabled(true);
        verify(view, timeout(3000)).showValidationError(anyString());
    }

    @Test
    @DisplayName(
            "Given an empty titulo field," +
            " when save is triggered," +
            " then an error message is shown and nothing is saved"
    )
    void shouldShowErrorWhenTituloIsEmpty() {
        when(view.getTitulo()).thenReturn("");

        saveAction.run();

        verify(view).showValidationError(anyString());
        verify(crudUseCase, never()).save(any());
    }

    @Test
    @DisplayName(
            "Given an empty ISBN field on save," +
            " when save is triggered," +
            " then an error message is shown and nothing is saved"
    )
    void shouldShowErrorWhenIsbnIsEmptyOnSave() {
        when(view.getTitulo()).thenReturn("Clean Code");
        when(view.getIsbn()).thenReturn("");

        saveAction.run();

        verify(view).showValidationError(anyString());
        verify(crudUseCase, never()).save(any());
    }

    @Test
    @DisplayName(
            "Given an empty autores field," +
            " when save is triggered," +
            " then an error message is shown and nothing is saved"
    )
    void shouldShowErrorWhenAutoresIsEmpty() {
        when(view.getTitulo()).thenReturn("Clean Code");
        when(view.getIsbn()).thenReturn(VALID_ISBN);
        when(view.getAutores()).thenReturn("");

        saveAction.run();

        verify(view).showValidationError(anyString());
        verify(crudUseCase, never()).save(any());
    }

    @Test
    @DisplayName(
            "Given a date in full ISO format instead of year only," +
            " when save is triggered," +
            " then an error message is shown and nothing is saved"
    )
    void shouldShowErrorWhenDateFormatIsInvalid() {
        when(view.getTitulo()).thenReturn("Clean Code");
        when(view.getIsbn()).thenReturn(VALID_ISBN);
        when(view.getAutores()).thenReturn("Robert Martin");
        when(view.getDataPublicacao()).thenReturn("2003-01-01");

        saveAction.run();

        verify(view).showValidationError(anyString());
        verify(crudUseCase, never()).save(any());
    }

    @Test
    @DisplayName(
            "Given a valid book without editora," +
            " when save is triggered," +
            " then book is persisted, onSuccess is called and view is closed"
    )
    void shouldSaveBookSuccessfully() {
        givenValidFormData();

        saveAction.run();

        verify(crudUseCase).save(any(Livro.class));
        verify(onSuccess).run();
        verify(view).close();
    }

    @Test
    @DisplayName(
            "Given a valid book with an editora not yet in the database," +
            " when save is triggered," +
            " then editora is created before saving"
    )
    void shouldCreateEditoraWhenNotFound() {
        givenValidFormData();
        when(view.getEditora()).thenReturn("Pearson");
        when(editoraUseCase.findOrCreate("Pearson")).thenReturn(new Editora("Pearson"));

        saveAction.run();

        verify(editoraUseCase).findOrCreate("Pearson");
        verify(crudUseCase).save(any(Livro.class));
    }

    @Test
    @DisplayName(
            "Given a valid book with an editora already in the database," +
            " when save is triggered," +
            " then existing editora is reused"
    )
    void shouldReuseExistingEditora() {
        givenValidFormData();
        Editora existing = new Editora("Pearson");
        existing.setId(1L);
        when(view.getEditora()).thenReturn("Pearson");
        when(editoraUseCase.findOrCreate("Pearson")).thenReturn(existing);

        saveAction.run();

        verify(editoraUseCase).findOrCreate("Pearson");
        verify(crudUseCase).save(any(Livro.class));
    }

    @Test
    @DisplayName(
            "Given an existing book being edited," +
            " when save is triggered," +
            " then the saved livro carries the original id"
    )
    void shouldPreserveIdWhenEditingBook() {
        Livro livro = livroWithId(42L);
        LivroFormPresenter presenter = new LivroFormPresenter(
                view, crudUseCase, searchUseCase, isbnLookupUseCase, editoraUseCase, onSuccess);
        presenter.initEdit(livro);

        givenValidFormData();

        ArgumentCaptor<Runnable> saveCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(view, atLeastOnce()).onSave(saveCaptor.capture());
        saveCaptor.getValue().run();

        ArgumentCaptor<Livro> livroCaptor = ArgumentCaptor.forClass(Livro.class);
        verify(crudUseCase).save(livroCaptor.capture());
        assertEquals(42L, livroCaptor.getValue().getId());
    }

    private void givenValidFormData() {
        when(view.getTitulo()).thenReturn("Clean Code");
        when(view.getIsbn()).thenReturn(VALID_ISBN);
        when(view.getAutores()).thenReturn("Robert Martin");
        when(view.getEditora()).thenReturn("");
        when(view.getDataPublicacao()).thenReturn("");
        when(view.getIdioma()).thenReturn("");
        when(view.getNumeroPaginas()).thenReturn("");
        when(view.getLivrosSemelhantes()).thenReturn(new ArrayList<>());
    }

    private Livro livroWithId(Long id) {
        Livro livro = new Livro();
        livro.setId(id);
        livro.setTitulo("Clean Code");
        livro.setIsbn(new ISBN(VALID_ISBN));
        return livro;
    }
}
