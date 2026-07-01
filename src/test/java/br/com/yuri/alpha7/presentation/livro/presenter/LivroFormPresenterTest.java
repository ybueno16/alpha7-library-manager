package br.com.yuri.alpha7.presentation.livro.presenter;

import br.com.yuri.alpha7.application.isbn.IsbnLookupUseCase;
import br.com.yuri.alpha7.application.livro.BookCrudUseCase;
import br.com.yuri.alpha7.application.livro.BookSearchUseCase;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LivroFormPresenterTest {

    @Mock private LivroFormView     view;
    @Mock private BookCrudUseCase   crudUseCase;
    @Mock private BookSearchUseCase searchUseCase;
    @Mock private IsbnLookupUseCase isbnLookupUseCase;

    private Runnable onSuccess;
    private Runnable isbnLookupAction;
    private Runnable saveAction;
    private Runnable cancelAction;
    private Runnable addSemelhanteAction;
    private Runnable removeSemelhanteAction;

    private static final String VALID_ISBN = "9780132350884";

    @BeforeEach
    void setUp() {
        onSuccess = mock(Runnable.class);
        new LivroFormPresenter(view, crudUseCase, searchUseCase, isbnLookupUseCase, onSuccess);

        ArgumentCaptor<Runnable> isbnCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(view).onIsbnLookup(isbnCaptor.capture());
        isbnLookupAction = isbnCaptor.getValue();

        ArgumentCaptor<Runnable> saveCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(view).onSave(saveCaptor.capture());
        saveAction = saveCaptor.getValue();

        ArgumentCaptor<Runnable> cancelCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(view).onCancel(cancelCaptor.capture());
        cancelAction = cancelCaptor.getValue();

        ArgumentCaptor<Runnable> addSemelhanteCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(view).onAddSemelhante(addSemelhanteCaptor.capture());
        addSemelhanteAction = addSemelhanteCaptor.getValue();

        ArgumentCaptor<Runnable> removeSemelhanteCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(view).onRemoveSemelhante(removeSemelhanteCaptor.capture());
        removeSemelhanteAction = removeSemelhanteCaptor.getValue();
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
                view, crudUseCase, searchUseCase, isbnLookupUseCase, onSuccess);

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
        when(view.getNumeroPaginas()).thenReturn("");
        when(view.getTitulo()).thenReturn("");

        saveAction.run();

        verify(view).showValidationError(anyString());
        verify(crudUseCase, never()).saveWithEditora(any(), any());
    }

    @Test
    @DisplayName(
            "Given an empty ISBN field on save," +
            " when save is triggered," +
            " then an error message is shown and nothing is saved"
    )
    void shouldShowErrorWhenIsbnIsEmptyOnSave() {
        when(view.getNumeroPaginas()).thenReturn("");
        when(view.getTitulo()).thenReturn("Clean Code");
        when(view.getIsbn()).thenReturn("");

        saveAction.run();

        verify(view).showValidationError(anyString());
        verify(crudUseCase, never()).saveWithEditora(any(), any());
    }

    @Test
    @DisplayName(
            "Given an empty autores field," +
            " when save is triggered," +
            " then an error message is shown and nothing is saved"
    )
    void shouldShowErrorWhenAutoresIsEmpty() {
        when(view.getNumeroPaginas()).thenReturn("");
        when(view.getTitulo()).thenReturn("Clean Code");
        when(view.getIsbn()).thenReturn(VALID_ISBN);
        when(view.getAutores()).thenReturn("");

        saveAction.run();

        verify(view).showValidationError(anyString());
        verify(crudUseCase, never()).saveWithEditora(any(), any());
    }

    @Test
    @DisplayName(
            "Given a date in full ISO format instead of year only," +
            " when save is triggered," +
            " then an error message is shown and nothing is saved"
    )
    void shouldShowErrorWhenDateFormatIsInvalid() {
        when(view.getNumeroPaginas()).thenReturn("");
        when(view.getTitulo()).thenReturn("Clean Code");
        when(view.getIsbn()).thenReturn(VALID_ISBN);
        when(view.getAutores()).thenReturn("Robert Martin");
        when(view.getDataPublicacao()).thenReturn("2003-01-01");

        saveAction.run();

        verify(view).showValidationError(anyString());
        verify(crudUseCase, never()).saveWithEditora(any(), any());
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

        verify(crudUseCase).saveWithEditora(any(Livro.class), anyString());
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

        saveAction.run();

        verify(crudUseCase).saveWithEditora(any(Livro.class), eq("Pearson"));
    }

    @Test
    @DisplayName(
            "Given a valid book with an editora already in the database," +
            " when save is triggered," +
            " then existing editora is reused"
    )
    void shouldReuseExistingEditora() {
        givenValidFormData();
        when(view.getEditora()).thenReturn("Pearson");

        saveAction.run();

        verify(crudUseCase).saveWithEditora(any(Livro.class), eq("Pearson"));
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
                view, crudUseCase, searchUseCase, isbnLookupUseCase, onSuccess);
        presenter.initEdit(livro);

        givenValidFormData();

        ArgumentCaptor<Runnable> saveCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(view, atLeastOnce()).onSave(saveCaptor.capture());
        saveCaptor.getValue().run();

        ArgumentCaptor<Livro> livroCaptor = ArgumentCaptor.forClass(Livro.class);
        verify(crudUseCase).saveWithEditora(livroCaptor.capture(), anyString());
        assertEquals(42L, livroCaptor.getValue().getId());
    }

    @Test
    @DisplayName(
            "Given the presenter is initialized," +
            " when the cancel action is triggered," +
            " then the view is closed"
    )
    void shouldCloseViewWhenCancelTriggered() {
        cancelAction.run();
        verify(view).close();
    }

    @Test
    @DisplayName(
            "Given the crudUseCase throws on save," +
            " when save is triggered with valid data," +
            " then an error message is shown and the view stays open"
    )
    void shouldShowErrorMessageWhenSaveFails() {
        givenValidFormData();
        when(crudUseCase.saveWithEditora(any(), any())).thenThrow(new RuntimeException("DB error"));

        saveAction.run();

        verify(view).showErrorMessage(anyString());
        verify(view, never()).close();
    }

    @Test
    @DisplayName(
            "Given a valid book with year date, idioma and page count," +
            " when save is triggered," +
            " then those optional fields are persisted on the livro"
    )
    void shouldSetOptionalFieldsWhenProvided() {
        when(view.getTitulo()).thenReturn("Clean Code");
        when(view.getIsbn()).thenReturn(VALID_ISBN);
        when(view.getAutores()).thenReturn("Robert Martin");
        when(view.getEditora()).thenReturn("");
        when(view.getDataPublicacao()).thenReturn("2003");
        when(view.getIdioma()).thenReturn("English");
        when(view.getNumeroPaginas()).thenReturn("431");
        when(view.getLivrosSemelhantes()).thenReturn(new ArrayList<>());

        saveAction.run();

        ArgumentCaptor<Livro> captor = ArgumentCaptor.forClass(Livro.class);
        verify(crudUseCase).saveWithEditora(captor.capture(), anyString());
        Livro saved = captor.getValue();
        assertEquals(LocalDate.of(2003, 1, 1), saved.getDataPublicacao());
        assertEquals("English", saved.getIdioma());
        assertEquals(431, saved.getNumeroPaginas());
    }

    @Test
    @DisplayName(
            "Given there are available books," +
            " when the user picks a semelhante," +
            " then the view list is updated with the chosen book"
    )
    void shouldAddChosenSemelhanteToView() {
        Livro semelhante = livroWithId(10L);
        when(searchUseCase.findAll()).thenReturn(Collections.singletonList(semelhante));
        when(view.getLivrosSemelhantes()).thenReturn(new ArrayList<>());
        when(view.pickSemelhante(anyList())).thenReturn(Optional.of(semelhante));

        addSemelhanteAction.run();

        verify(view).setLivrosSemelhantes(Collections.singletonList(semelhante));
    }

    @Test
    @DisplayName(
            "Given available books are shown for selection," +
            " when the user cancels the picker," +
            " then the semelhantes list is not updated"
    )
    void shouldNotUpdateListWhenUserCancelsSemelhantePicker() {
        when(searchUseCase.findAll()).thenReturn(Collections.singletonList(livroWithId(10L)));
        when(view.getLivrosSemelhantes()).thenReturn(new ArrayList<>());
        when(view.pickSemelhante(anyList())).thenReturn(Optional.empty());

        addSemelhanteAction.run();

        verify(view, never()).setLivrosSemelhantes(anyList());
    }

    @Test
    @DisplayName(
            "Given a semelhante is selected in the view," +
            " when the remove action is triggered," +
            " then it is removed from the list"
    )
    void shouldRemoveSelectedSemelhanteFromList() {
        Livro semelhante = livroWithId(10L);
        when(view.getSelectedSemelhante()).thenReturn(Optional.of(semelhante));
        when(view.getLivrosSemelhantes()).thenReturn(new ArrayList<>(Collections.singletonList(semelhante)));

        removeSemelhanteAction.run();

        verify(view).setLivrosSemelhantes(Collections.emptyList());
    }

    @Test
    @DisplayName(
            "Given no semelhante is selected in the view," +
            " when the remove action is triggered," +
            " then the list is not modified"
    )
    void shouldNotModifyListWhenNoSemelhanteIsSelected() {
        when(view.getSelectedSemelhante()).thenReturn(Optional.empty());

        removeSemelhanteAction.run();

        verify(view, never()).setLivrosSemelhantes(anyList());
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
