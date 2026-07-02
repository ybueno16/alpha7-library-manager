package br.com.yuri.alpha7.application.livro;

import br.com.yuri.alpha7.application.UnitOfWork;
import br.com.yuri.alpha7.domain.autor.model.Autor;
import br.com.yuri.alpha7.domain.autor.repository.AutorRepository;
import br.com.yuri.alpha7.domain.editora.repository.EditoraRepository;
import br.com.yuri.alpha7.domain.exception.BookNotFoundException;
import br.com.yuri.alpha7.domain.exception.IsbnInvalidoException;
import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.repository.LivroRepository;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.yuri.alpha7.domain.editora.model.Editora;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookCrudUseCaseTest {

    @Mock
    private LivroRepository   livroRepository;
    @Mock
    private AutorRepository   autorRepository;
    @Mock
    private EditoraRepository editoraRepository;
    @Mock
    private UnitOfWork        unitOfWork;

    @InjectMocks
    private BookCrudUseCase useCase;

    private static final ISBN VALID_ISBN = new ISBN("9780132350884");

    @Test
    @DisplayName(
            "Given a book with a new ISBN," +
            " when save is called," +
            " then book is persisted via repository"
    )
    void shouldPersistBookWhenIsbnIsNew() {
        Livro livro = bookWithIsbn(VALID_ISBN);
        when(livroRepository.findByIsbn(VALID_ISBN)).thenReturn(Optional.empty());
        when(livroRepository.save(livro)).thenReturn(livro);

        Livro result = useCase.save(livro);

        assertNotNull(result);
        verify(livroRepository).save(livro);
    }

    @Test
    @DisplayName(
            "Given a book with a duplicate ISBN," +
            " when save is called," +
            " then IsbnInvalidoException is thrown"
    )
    void shouldThrowExceptionWhenIsbnIsDuplicated() {
        Livro livro = bookWithIsbn(VALID_ISBN);
        when(livroRepository.findByIsbn(VALID_ISBN)).thenReturn(Optional.of(livro));

        assertThrows(IsbnInvalidoException.class, () -> useCase.save(livro));
        verify(livroRepository, never()).save(any());
    }

    @Test
    @DisplayName(
            "Given an existing book," +
            " when findById is called with its id," +
            " then book is returned"
    )
    void shouldReturnBookWhenFoundById() {
        Livro livro = bookWithIsbn(VALID_ISBN);
        when(livroRepository.findById(1L)).thenReturn(Optional.of(livro));

        Livro result = useCase.findById(1L);

        assertEquals(livro, result);
    }

    @Test
    @DisplayName(
            "Given no book with the given id," +
            " when findById is called," +
            " then BookNotFoundException is thrown"
    )
    void shouldThrowBookNotFoundExceptionWhenIdDoesNotExist() {
        when(livroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> useCase.findById(99L));
    }

    @Test
    @DisplayName(
            "Given an existing book," +
            " when delete is called," +
            " then deletion is delegated to the repository"
    )
    void shouldDelegateDeleteToRepositoryWhenBookExists() {
        assertDoesNotThrow(() -> useCase.delete(1L));
        verify(livroRepository).delete(1L);
    }

    @Test
    @DisplayName(
            "Given no book with the given id," +
            " when delete is called," +
            " then BookNotFoundException is thrown"
    )
    void shouldThrowBookNotFoundExceptionWhenDeletingNonExistentBook() {
        doThrow(new BookNotFoundException("Livro não encontrado")).when(livroRepository).delete(99L);

        assertThrows(BookNotFoundException.class, () -> useCase.delete(99L));
        verify(livroRepository).delete(99L);
    }

    @Test
    @DisplayName(
            "Given a book and an existing editora name," +
            " when saveWithEditora is called," +
            " then the editora is resolved and the book is saved within a single transaction"
    )
    void shouldResolveEditoraAndSaveBookInSingleTransaction() {
        Livro livro    = bookWithIsbn(VALID_ISBN);
        Editora editora = new Editora("Prentice Hall");

        doAnswer(inv -> ((Supplier<?>) inv.getArgument(0)).get())
                .when(unitOfWork).execute(any(Supplier.class));
        when(editoraRepository.findByNome("Prentice Hall")).thenReturn(Optional.of(editora));
        when(livroRepository.findByIsbn(VALID_ISBN)).thenReturn(Optional.empty());
        when(livroRepository.save(livro)).thenReturn(livro);

        Livro saved = useCase.saveWithEditora(livro, "Prentice Hall");

        assertSame(livro, saved);
        verify(editoraRepository).findByNome("Prentice Hall");
        verify(livroRepository).save(livro);
    }

    @Test
    @DisplayName(
            "Given a book and a new editora name not yet in the database," +
            " when saveWithEditora is called," +
            " then the editora is created and the book is saved within a single transaction"
    )
    void shouldCreateEditoraWhenNotFoundAndSaveBook() {
        Livro livro    = bookWithIsbn(VALID_ISBN);
        Editora nova    = new Editora("NovaEditora");

        doAnswer(inv -> ((Supplier<?>) inv.getArgument(0)).get())
                .when(unitOfWork).execute(any(Supplier.class));
        when(editoraRepository.findByNome("NovaEditora")).thenReturn(Optional.empty());
        when(editoraRepository.save(any(Editora.class))).thenReturn(nova);
        when(livroRepository.findByIsbn(VALID_ISBN)).thenReturn(Optional.empty());
        when(livroRepository.save(livro)).thenReturn(livro);

        Livro saved = useCase.saveWithEditora(livro, "NovaEditora");

        assertSame(livro, saved);
        verify(editoraRepository).save(any(Editora.class));
        verify(livroRepository).save(livro);
    }

    @Test
    @DisplayName(
            "Given a book with an author typed by name that already exists in the database," +
            " when saveWithEditora is called," +
            " then the existing author is resolved by name and no new author is created"
    )
    void shouldResolveExistingAutorByNameWhenSavingBook() {
        Autor existing = new Autor("Robert Martin");
        existing.setId(1L);
        Livro livro = bookWithIsbn(VALID_ISBN);
        livro.setAutores(Collections.singletonList(new Autor("Robert Martin")));

        doAnswer(inv -> ((Supplier<?>) inv.getArgument(0)).get())
                .when(unitOfWork).execute(any(Supplier.class));
        when(autorRepository.findByNome("Robert Martin")).thenReturn(Optional.of(existing));
        when(livroRepository.findByIsbn(VALID_ISBN)).thenReturn(Optional.empty());
        when(livroRepository.save(livro)).thenReturn(livro);

        useCase.saveWithEditora(livro, null);

        verify(autorRepository).findByNome("Robert Martin");
        verify(autorRepository, never()).save(any(Autor.class));
        verify(livroRepository).save(livro);
    }

    @Test
    @DisplayName(
            "Given a book with a new author name not yet in the database," +
            " when saveWithEditora is called," +
            " then the author is created and associated to the book"
    )
    void shouldCreateNewAutorWhenNotFoundByName() {
        Autor novoAutor = new Autor("Novo Autor");
        Autor savedAutor = new Autor("Novo Autor");
        savedAutor.setId(99L);
        Livro livro = bookWithIsbn(VALID_ISBN);
        livro.setAutores(Collections.singletonList(novoAutor));

        doAnswer(inv -> ((Supplier<?>) inv.getArgument(0)).get())
                .when(unitOfWork).execute(any(Supplier.class));
        when(autorRepository.findByNome("Novo Autor")).thenReturn(Optional.empty());
        when(autorRepository.save(novoAutor)).thenReturn(savedAutor);
        when(livroRepository.findByIsbn(VALID_ISBN)).thenReturn(Optional.empty());
        when(livroRepository.save(livro)).thenReturn(livro);

        useCase.saveWithEditora(livro, null);

        verify(autorRepository).save(novoAutor);
    }

    private Livro bookWithIsbn(ISBN isbn) {
        Livro livro = new Livro();
        livro.setTitulo("Clean Code");
        livro.setIsbn(isbn);
        return livro;
    }
}
