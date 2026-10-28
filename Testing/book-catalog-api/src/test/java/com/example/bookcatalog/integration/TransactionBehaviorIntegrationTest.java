package com.example.bookcatalog.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.bookcatalog.exception.TransactionLabCheckedException;
import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.repository.BookRepository;
import com.example.bookcatalog.repository.TransactionAuditRepository;
import com.example.bookcatalog.service.BookService;
import com.example.bookcatalog.service.StockTransferService;
import com.example.bookcatalog.support.TestPostgresConfiguration;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.UnexpectedRollbackException;

@SpringBootTest
@Import(TestPostgresConfiguration.class)
@ActiveProfiles("test")
class TransactionBehaviorIntegrationTest {

    @Autowired
    StockTransferService transfers;

    @Autowired
    BookService bookService;

    @Autowired
    BookRepository books;

    @Autowired
    TransactionAuditRepository audits;

    private Long fromId;
    private Long toId;

    @BeforeEach
    void setUp() {
        audits.deleteAllInBatch();
        books.deleteAllInBatch();
        fromId = books.saveAndFlush(book("tx-from", 10)).getId();
        toId = books.saveAndFlush(book("tx-to", 5)).getId();
    }

    @Test
    void runtimeFailure_rollsBackStockAndRequiredAudit() {
        assertThatThrownBy(() ->
                transfers.transferThenRuntimeRollback(fromId, toId, 3))
                .isInstanceOf(IllegalStateException.class);

        assertStocks(10, 5);
        assertThat(audits.count()).isZero();
    }

    @Test
    void checkedFailureWithDefaultRules_commitsStockAndAudit() {
        assertThatThrownBy(() ->
                transfers.checkedFailureDefault(fromId, toId, 3))
                .isInstanceOf(TransactionLabCheckedException.class);

        assertStocks(7, 8);
        assertThat(audits.findAll())
                .singleElement()
                .satisfies(audit -> assertThat(audit.getEventType())
                        .isEqualTo("CHECKED_DEFAULT_COMMITTED"));
    }

    @Test
    void requiresNewAudit_outerFailureRollsBackStockButAuditSurvives() {
        assertThatThrownBy(() ->
                transfers.requiresNewAuditThenFail(fromId, toId, 3))
                .isInstanceOf(IllegalStateException.class);

        assertStocks(10, 5);
        assertThat(audits.findAll())
                .singleElement()
                .satisfies(audit -> assertThat(audit.getEventType())
                        .isEqualTo("TRANSFER_ATTEMPT"));
    }

    @Test
    void delete_intentionalCheckedFailure_rollsBackFlushedDelete() {
        assertThatThrownBy(() -> bookService.delete(fromId))
                .isInstanceOf(TransactionLabCheckedException.class)
                .hasMessage("Intentional failure after flush");

        assertThat(books.existsById(fromId)).isTrue();
    }


    @Test
    void successfulTransfer_commitsStockAndRequiredAudit() {
        transfers.transferSuccessfully(fromId, toId, 3);
        assertStocks(7, 8);
        assertThat(audits.findAll()).singleElement().satisfies(audit ->
                assertThat(audit.getEventType()).isEqualTo("TRANSFER_COMPLETED"));
    }

    @Test
    void flushThenRuntimeFailure_stillRollsBackStock() {
        assertThatThrownBy(() -> transfers.flushThenRuntimeRollback(fromId, toId, 3))
                .isInstanceOf(IllegalStateException.class);
        assertStocks(10, 5);
        assertThat(audits.count()).isZero();
    }

    @Test
    void checkedFailureWithRollbackFor_rollsBackStockAndAudit() {
        assertThatThrownBy(() -> transfers.checkedFailureWithRollback(fromId, toId, 3))
                .isInstanceOf(TransactionLabCheckedException.class);
        assertStocks(10, 5);
        assertThat(audits.count()).isZero();
    }

    @Test
    void caughtRequiredFailure_leavesTransactionRollbackOnly() {
        assertThatThrownBy(() -> transfers.catchInnerRequiredFailure(fromId, toId, 3))
                .isInstanceOf(UnexpectedRollbackException.class);
        assertStocks(10, 5);
        assertThat(audits.count()).isZero();
    }

    private void assertStocks(int expectedFrom, int expectedTo) {
        assertThat(books.findById(fromId).orElseThrow().getStock())
                .isEqualTo(expectedFrom);
        assertThat(books.findById(toId).orElseThrow().getStock())
                .isEqualTo(expectedTo);
    }

    private static Book book(String isbn, int stock) {
        return new Book(isbn, "Transaction Test", "Author",
                new BigDecimal("10.00"), stock);
    }
}
