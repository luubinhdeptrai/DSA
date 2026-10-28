package com.example.bookcatalog.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.repository.BookRepository;
import com.example.bookcatalog.service.BookService;
import com.example.bookcatalog.support.TestPostgresConfiguration;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(TestPostgresConfiguration.class)
@ActiveProfiles("test")
class BookServiceMethodSecurityIntegrationTest {

    @Autowired
    BookService service;

    @Autowired
    BookRepository books;

    private Long bookId;

    @BeforeEach
    void setUp() {
        books.deleteAllInBatch();
        bookId = books.saveAndFlush(new Book(
                "method-security-isbn", "Title", "Author",
                new BigDecimal("15.00"), 5)).getId();
    }

    @Test
    @WithMockUser(roles = "USER")
    void setStock_user_isDeniedByMethodProxy() {
        assertThatThrownBy(() -> service.setStock(bookId, 9))
                .isInstanceOf(AccessDeniedException.class);

        assertThat(books.findById(bookId).orElseThrow().getStock())
                .isEqualTo(5);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void setStock_admin_reachesTransactionalBusinessMethod() {
        service.setStock(bookId, 9);

        assertThat(books.findById(bookId).orElseThrow().getStock())
                .isEqualTo(9);
    }
}
