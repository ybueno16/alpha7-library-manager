package br.com.yuri.alpha7.infra.client.openlibrary;

import br.com.yuri.alpha7.domain.livro.model.Livro;
import br.com.yuri.alpha7.domain.livro.vo.ISBN;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CachedBookLookupTest {

    @Test
    @DisplayName(
            "Given a book," +
            " when found is called," +
            " then toOptional returns the book"
    )
    void shouldReturnBookWhenFound() {
        Livro livro = new Livro();
        livro.setTitulo("Clean Code");
        livro.setIsbn(new ISBN("9780132350884"));

        CachedBookLookup result = CachedBookLookup.found(livro);

        assertEquals(Optional.of(livro), result.toOptional());
    }

    @Test
    @DisplayName(
            "Given no book," +
            " when notFound is called," +
            " then toOptional returns empty"
    )
    void shouldReturnEmptyWhenNotFound() {
        CachedBookLookup result = CachedBookLookup.notFound();

        assertEquals(Optional.empty(), result.toOptional());
    }

    @Test
    @DisplayName(
            "Given a found lookup result," +
            " when serialized and deserialized," +
            " then the book data is preserved"
    )
    void shouldSurviveSerializationRoundTrip() throws Exception {
        Livro livro = new Livro();
        livro.setTitulo("Effective Java");
        livro.setIsbn(new ISBN("9780134685991"));
        CachedBookLookup original = CachedBookLookup.found(livro);

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(byteStream)) {
            out.writeObject(original);
        }

        CachedBookLookup deserialized;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(byteStream.toByteArray()))) {
            deserialized = (CachedBookLookup) in.readObject();
        }

        assertEquals(original.toOptional(), deserialized.toOptional());
    }
}
