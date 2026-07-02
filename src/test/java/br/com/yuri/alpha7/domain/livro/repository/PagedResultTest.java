package br.com.yuri.alpha7.domain.livro.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class PagedResultTest {

    @Test
    @DisplayName(
            "Given a paged result with 10 items and page size 3," +
            " when totalPages is called," +
            " then ceiling division is applied"
    )
    void shouldCalculateTotalPagesWithCeilingDivision() {
        PagedResult<String> result = new PagedResult<>(Collections.emptyList(), 10);
        assertEquals(4, result.totalPages(3));
    }

    @Test
    @DisplayName(
            "Given a paged result with total count divisible by page size," +
            " when totalPages is called," +
            " then exact division is returned"
    )
    void shouldCalculateExactTotalPages() {
        PagedResult<String> result = new PagedResult<>(Collections.emptyList(), 9);
        assertEquals(3, result.totalPages(3));
    }

    @Test
    @DisplayName(
            "Given a paged result with zero or negative page size," +
            " when totalPages is called," +
            " then 1 is returned"
    )
    void shouldReturnOneWhenPageSizeIsZeroOrNegative() {
        PagedResult<String> result = new PagedResult<>(Collections.emptyList(), 10);
        assertEquals(1, result.totalPages(0));
        assertEquals(1, result.totalPages(-5));
    }

    @Test
    @DisplayName(
            "Given a paged result," +
            " when getItems and getTotalCount are called," +
            " then the correct values are returned"
    )
    void shouldReturnItemsAndTotalCount() {
        PagedResult<String> result = new PagedResult<>(Arrays.asList("a", "b"), 100);
        assertEquals(2, result.getItems().size());
        assertEquals(100, result.getTotalCount());
    }
}
