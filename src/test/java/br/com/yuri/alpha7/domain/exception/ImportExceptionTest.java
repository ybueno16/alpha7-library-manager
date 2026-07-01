package br.com.yuri.alpha7.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImportExceptionTest {

    @Test
    @DisplayName(
            "Given a message string," +
            " when ImportException is created with it," +
            " then the message is accessible via getMessage"
    )
    void shouldStoreMessageWhenCreatedWithMessage() {
        ImportException ex = new ImportException("parse error");
        assertEquals("parse error", ex.getMessage());
    }

    @Test
    @DisplayName(
            "Given a message and a root cause," +
            " when ImportException is created with both," +
            " then both message and cause are accessible"
    )
    void shouldStoreMessageAndCauseWhenCreatedWithBoth() {
        Throwable cause = new RuntimeException("root cause");
        ImportException ex = new ImportException("wrapped error", cause);
        assertEquals("wrapped error", ex.getMessage());
        assertSame(cause, ex.getCause());
    }
}
