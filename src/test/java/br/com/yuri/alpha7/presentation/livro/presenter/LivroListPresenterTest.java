package br.com.yuri.alpha7.presentation.livro.presenter;

import br.com.yuri.alpha7.application.importacao.ImportUseCase;
import br.com.yuri.alpha7.application.isbn.IsbnLookupUseCase;
import br.com.yuri.alpha7.application.livro.BookCrudUseCase;
import br.com.yuri.alpha7.application.livro.BookExportUseCase;
import br.com.yuri.alpha7.application.livro.BookSearchUseCase;
import br.com.yuri.alpha7.domain.exception.BookNotFoundException;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.presentation.livro.view.LivroListView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LivroListPresenterTest {

    @Mock private LivroListView     view;
    @Mock private BookSearchUseCase searchUseCase;
    @Mock private BookCrudUseCase   crudUseCase;
    @Mock private BookExportUseCase exportUseCase;
    @Mock private ImportUseCase     importUseCase;
    @Mock private IsbnLookupUseCase isbnLookupUseCase;

    private Runnable searchAction;
    private Runnable deleteAction;
    private Runnable editAction;
    private LivroListPresenter presenter;

    @BeforeEach
    void setUp() {
        presenter = new LivroListPresenter(
                null, view, searchUseCase, crudUseCase, exportUseCase, importUseCase, isbnLookupUseCase);

        ArgumentCaptor<Runnable> searchCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(view).onSearch(searchCaptor.capture());
        searchAction = searchCaptor.getValue();

        ArgumentCaptor<Runnable> deleteCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(view).onDelete(deleteCaptor.capture());
        deleteAction = deleteCaptor.getValue();

        ArgumentCaptor<Runnable> editCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(view).onEdit(editCaptor.capture());
        editAction = editCaptor.getValue();
    }

    @Test
    @DisplayName(
            "Given a request to load books," +
            " when loadLivros is called," +
            " then all books are passed to the view"
    )
    void shouldShowAllLivrosOnLoad() {
        List<Livro> livros = Arrays.asList(new Livro(), new Livro());
        when(searchUseCase.findAll()).thenReturn(livros);

        presenter.loadLivros();

        verify(view).showLivros(livros);
    }

    @Test
    @DisplayName(
            "Given an empty search term," +
            " when search is triggered," +
            " then all books are returned"
    )
    void shouldSearchAllWhenTermIsEmpty() {
        List<Livro> livros = Collections.singletonList(new Livro());
        when(view.getSearchTerm()).thenReturn("   ");
        when(searchUseCase.findAll()).thenReturn(livros);

        searchAction.run();

        verify(searchUseCase).findAll();
        verify(searchUseCase, never()).findByFiltro(anyString());
        verify(view).showLivros(livros);
    }

    @Test
    @DisplayName(
            "Given a non-empty search term," +
            " when search is triggered," +
            " then filtered books are returned"
    )
    void shouldSearchByTermWhenTermIsNotEmpty() {
        List<Livro> livros = Collections.singletonList(new Livro());
        when(view.getSearchTerm()).thenReturn("Clean");
        when(searchUseCase.findByFiltro("Clean")).thenReturn(livros);

        searchAction.run();

        verify(searchUseCase).findByFiltro("Clean");
        verify(searchUseCase, never()).findAll();
        verify(view).showLivros(livros);
    }

    @Test
    @DisplayName(
            "Given no book selected," +
            " when delete is triggered," +
            " then an error message is shown and nothing is deleted"
    )
    void shouldShowErrorWhenNoLivroSelectedForDelete() {
        when(view.getSelectedLivros()).thenReturn(Collections.emptyList());

        deleteAction.run();

        verify(view).showErrorMessage(anyString());
        verify(crudUseCase, never()).delete(any());
    }

    @Test
    @DisplayName(
            "Given a book is selected and user cancels confirmation," +
            " when delete is triggered," +
            " then nothing is deleted"
    )
    void shouldNotDeleteWhenUserCancels() {
        when(view.getSelectedLivros()).thenReturn(Collections.singletonList(livroWithId(1L)));
        when(view.confirm(anyString())).thenReturn(false);

        deleteAction.run();

        verify(crudUseCase, never()).delete(any());
    }

    @Test
    @DisplayName(
            "Given a book is selected and user confirms deletion," +
            " when delete is triggered," +
            " then book is deleted and list is refreshed"
    )
    void shouldDeleteAndRefreshWhenConfirmed() {
        when(view.getSelectedLivros()).thenReturn(Collections.singletonList(livroWithId(5L)));
        when(view.confirm(anyString())).thenReturn(true);
        when(searchUseCase.findAll()).thenReturn(Collections.emptyList());

        deleteAction.run();

        verify(crudUseCase).delete(5L);
        verify(view).showLivros(any());
    }

    @Test
    @DisplayName(
            "Given multiple books are selected and user confirms," +
            " when delete is triggered," +
            " then all selected books are deleted and list is refreshed"
    )
    void shouldDeleteAllSelectedLivrosWhenConfirmed() {
        when(view.getSelectedLivros()).thenReturn(Arrays.asList(livroWithId(1L), livroWithId(2L), livroWithId(3L)));
        when(view.confirm(anyString())).thenReturn(true);
        when(searchUseCase.findAll()).thenReturn(Collections.emptyList());

        deleteAction.run();

        verify(crudUseCase).delete(1L);
        verify(crudUseCase).delete(2L);
        verify(crudUseCase).delete(3L);
        verify(view).showLivros(any());
    }

    @Test
    @DisplayName(
            "Given no book selected," +
            " when edit is triggered," +
            " then no dialog is opened"
    )
    void shouldDoNothingWhenNoLivroSelectedForEdit() {
        when(view.getSelectedLivro()).thenReturn(Optional.empty());

        editAction.run();

        verify(view, never()).showErrorMessage(anyString());
    }

    @Test
    @DisplayName(
            "Given a book selected for edit but not found by id," +
            " when edit is triggered," +
            " then an error is shown and the list is refreshed"
    )
    void shouldShowErrorAndRefreshWhenSelectedLivroNotFoundById() {
        when(view.getSelectedLivro()).thenReturn(Optional.of(livroWithId(1L)));
        when(crudUseCase.findById(1L)).thenThrow(new BookNotFoundException("Livro não encontrado"));
        when(searchUseCase.findAll()).thenReturn(Collections.emptyList());

        editAction.run();

        verify(view).showErrorMessage(anyString());
        verify(view).showLivros(any());
    }

    @Test
    @DisplayName(
            "Given a book fails to delete," +
            " when delete is confirmed," +
            " then an error message is shown after the list is refreshed"
    )
    void shouldShowErrorMessageWhenDeletionThrows() {
        when(view.getSelectedLivros()).thenReturn(Collections.singletonList(livroWithId(7L)));
        when(view.confirm(anyString())).thenReturn(true);
        when(searchUseCase.findAll()).thenReturn(Collections.emptyList());
        doThrow(new RuntimeException("constraint violation")).when(crudUseCase).delete(7L);

        deleteAction.run();

        verify(view).showLivros(any());
        verify(view).showErrorMessage(anyString());
    }

    private Livro livroWithId(Long id) {
        Livro livro = new Livro();
        livro.setId(id);
        livro.setTitulo("Livro " + id);
        return livro;
    }
}
