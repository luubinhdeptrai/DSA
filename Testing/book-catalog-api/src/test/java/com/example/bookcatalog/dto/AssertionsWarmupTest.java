package com.example.bookcatalog.dto;

import com.example.bookcatalog.model.Book;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AssertionsWarmupTest {
    @Test
    void request_normalizesTextAndPreservesNumbers() {
        BigDecimal price = new BigDecimal("19.99");
        BookRequest request = new BookRequest("  isbn-1  ", "  Title  ",
                "  Author  ", price, 3);
        assertAll(
                () -> assertEquals("isbn-1", request.isbn()),
                () -> assertEquals("Title", request.title()),
                () -> assertEquals("Author", request.author()),
                () -> assertEquals(price, request.price()),
                () -> assertEquals(Integer.valueOf(3), request.stock()));
    }

    @Test
    void response_unsavedBook_hasNoUsableId() {
        Book unsaved = new Book("isbn-1", "Title", "Author",
                new BigDecimal("19.99"), 3);
        assertNull(unsaved.getId());
        assertThrows(NullPointerException.class, () -> BookResponse.from(unsaved));
    }
}
