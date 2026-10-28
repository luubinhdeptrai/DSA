package com.example.bookcatalog.dto;

import java.math.BigDecimal;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class ParameterizedBookRequestTest {
    @ParameterizedTest
    @CsvSource({"'Title', Title", "'  Title  ', Title", "' A ', A"})
    void title_normalizesEachInput(String input, String expected) {
        assertEquals(expected, request(input).title());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    void title_nullOrBlank_remainsNullOrBecomesEmpty(String input) {
        String actual = request(input).title();
        if (input == null) assertNull(actual);
        else assertEquals("", actual);
    }

    private static BookRequest request(String title) {
        return new BookRequest("isbn-1", title, "Author", new BigDecimal("1.00"), 1);
    }
}
