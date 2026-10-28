package com.example.bookcatalog.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class BookRequestTest {

    @Test
    void constructor_textValues_trimsThem() {
        BookRequest request = new BookRequest(
                "  isbn-1  ", "  Clean Code  ", "  Robert Martin  ",
                new BigDecimal("19.99"), 3);

        assertThat(request.isbn()).isEqualTo("isbn-1");
        assertThat(request.title()).isEqualTo("Clean Code");
        assertThat(request.author()).isEqualTo("Robert Martin");
        assertThat(request.price()).isEqualByComparingTo("19.99");
        assertThat(request.stock()).isEqualTo(3);
    }

    @Test
    void constructor_nullText_preservesNullWithoutRunningValidation() {
        BookRequest request = new BookRequest(null, null, null,
                new BigDecimal("1.00"), 0);
        assertThat(request.isbn()).isNull();
        assertThat(request.title()).isNull();
        assertThat(request.author()).isNull();
    }
}
