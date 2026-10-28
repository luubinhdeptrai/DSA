package com.example.bookcatalog.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.bookcatalog.model.AppUser;
import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.support.TestPostgresConfiguration;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestPostgresConfiguration.class)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@ActiveProfiles("test")
class RepositoryPostgresIntegrationTest {

    @Autowired
    BookRepository books;

    @Autowired
    AppUserRepository users;

    @Autowired
    EntityManager entityManager;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void startup_flywayAppliedAllSixMigrations() {
        Integer count = jdbc.queryForObject(
                "select count(*) from flyway_schema_history where success",
                Integer.class);

        assertThat(count).isEqualTo(6);
        assertThat(jdbc.queryForList(
                "select version from flyway_schema_history where success and type = 'SQL' order by installed_rank",
                String.class)).containsExactly("1", "2", "3", "4", "5", "6");
    }

    @Test
    void save_flushClearAndFind_roundTripsThroughPostgres() {
        Book saved = books.saveAndFlush(book("round-trip-isbn", "Author A"));
        Long id = saved.getId();
        entityManager.clear();

        Book reloaded = books.findById(id).orElseThrow();

        assertThat(reloaded.getIsbn()).isEqualTo("round-trip-isbn");
        assertThat(reloaded.getStock()).isEqualTo(10);
    }

    @Test
    void findByUsername_existingUser_usesRealMappingAndDerivedQuery() {
        users.saveAndFlush(new AppUser(
                "repository-user", "stored-hash", "USER"));
        entityManager.clear();

        AppUser found = users.findByUsername("repository-user")
                .orElseThrow();

        assertThat(found.getPasswordHash()).isEqualTo("stored-hash");
        assertThat(found.getRole()).isEqualTo("USER");
    }

    @Test
    void flush_dirtyEntityWithDuplicateIsbn_exposesDatabaseConstraint() {
        Book first = books.saveAndFlush(book("unique-isbn-1", "Author A"));
        Book second = books.saveAndFlush(book("unique-isbn-2", "Author B"));
        second.replace(first.getIsbn(), second.getTitle(), second.getAuthor(),
                second.getPrice(), second.getStock());
        books.save(second);

        assertThatThrownBy(books::flush)
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void flush_invalidRole_exposesPostgresCheckConstraint() {
        assertThatThrownBy(() -> users.saveAndFlush(
                new AppUser("bad-role-user", "stored-hash", "SUPERUSER")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }


    @Test
    void findByUsername_absentUser_returnsEmptyAndExistenceMatches() {
        assertThat(users.findByUsername("absent-repository-user")).isEmpty();
        assertThat(users.existsByUsername("absent-repository-user")).isFalse();
        users.saveAndFlush(new AppUser("present-repository-user", "stored-hash", "USER"));
        entityManager.clear();
        assertThat(users.existsByUsername("present-repository-user")).isTrue();
    }

    @Test
    void findByAuthor_exactFilterAndSortedPages_returnOnlyMatchingRows() {
        books.saveAndFlush(book("author-a1", "Author A"));
        books.saveAndFlush(book("other-author", "Author B"));
        books.saveAndFlush(book("author-a2", "Author A"));
        books.saveAndFlush(book("author-a3", "Author A"));
        entityManager.clear();

        assertThat(books.findByAuthor("Author A", PageRequest.of(0, 2, Sort.by("id"))))
                .extracting(Book::getIsbn).containsExactly("author-a1", "author-a2");
        assertThat(books.findByAuthor("Author A", PageRequest.of(1, 2, Sort.by("id"))))
                .extracting(Book::getIsbn).containsExactly("author-a3");
        assertThat(books.findByAuthor("Missing Author", PageRequest.of(0, 2, Sort.by("id"))))
                .isEmpty();
    }

    @Test
    void saveAndFlush_duplicateUsername_exposesUniqueConstraint() {
        users.saveAndFlush(new AppUser("duplicate-user", "stored-hash", "USER"));
        assertThatThrownBy(() -> users.saveAndFlush(
                new AppUser("duplicate-user", "another-hash", "USER")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private static Book book(String isbn, String author) {
        return new Book(isbn, "Repository Test", author,
                new BigDecimal("20.00"), 10);
    }
}
