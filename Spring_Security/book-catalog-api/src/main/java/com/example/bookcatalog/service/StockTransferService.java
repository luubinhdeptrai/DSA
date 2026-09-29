package com.example.bookcatalog.service;

import com.example.bookcatalog.exception.TransactionLabCheckedException;
import com.example.bookcatalog.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockTransferService {

    private final BookStockService stockService;
    private final TransactionAuditService auditService;
    private final BookRepository bookRepository;

    public StockTransferService(
            BookStockService stockService,
            TransactionAuditService auditService,
            BookRepository bookRepository) {
        this.stockService = stockService;
        this.auditService = auditService;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public void transferSuccessfully(
            long fromBookId,
            long toBookId,
            int amount) {
        moveStock(fromBookId, toBookId, amount);
        auditService.recordRequired(
                "TRANSFER_COMPLETED",
                details(fromBookId, toBookId, amount));
    }

    @Transactional
    public void transferThenRuntimeRollback(
            long fromBookId,
            long toBookId,
            int amount) {
        moveStock(fromBookId, toBookId, amount);
        auditService.recordRequired(
                "RUNTIME_FAILURE_WILL_ROLL_BACK",
                details(fromBookId, toBookId, amount));

        throw new IllegalStateException(
                "Intentional runtime failure after both changes");
    }

    @Transactional
    public void flushThenRuntimeRollback(
            long fromBookId,
            long toBookId,
            int amount) {
        moveStock(fromBookId, toBookId, amount);
        bookRepository.flush();

        throw new IllegalStateException(
                "Intentional runtime failure after flush");
    }

    @Transactional
    public void checkedFailureDefault(
            long fromBookId,
            long toBookId,
            int amount) throws TransactionLabCheckedException {
        moveStock(fromBookId, toBookId, amount);
        auditService.recordRequired(
                "CHECKED_DEFAULT_COMMITTED",
                details(fromBookId, toBookId, amount));

        throw new TransactionLabCheckedException(
                "Intentional checked failure with default rules");
    }

    @Transactional(rollbackFor = TransactionLabCheckedException.class)
    public void checkedFailureWithRollback(
            long fromBookId,
            long toBookId,
            int amount) throws TransactionLabCheckedException {
        moveStock(fromBookId, toBookId, amount);
        auditService.recordRequired(
                "CHECKED_FAILURE_WILL_ROLL_BACK",
                details(fromBookId, toBookId, amount));

        throw new TransactionLabCheckedException(
                "Intentional checked failure with rollbackFor");
    }

    @Transactional
    public void catchInnerRequiredFailure(
            long fromBookId,
            long toBookId,
            int amount) {
        moveStock(fromBookId, toBookId, amount);

        try {
            auditService.recordRequiredThenFail(
                    "INNER_REQUIRED_FAILURE",
                    details(fromBookId, toBookId, amount));
        } catch (IllegalStateException expected) {
            // The inner REQUIRED interceptor has already marked the shared
            // transaction rollback-only. Catching does not clear that flag.
        }
    }

    @Transactional
    public void requiresNewAuditThenFail(
            long fromBookId,
            long toBookId,
            int amount) {
        moveStock(fromBookId, toBookId, amount);

        auditService.recordRequiresNew(
                "TRANSFER_ATTEMPT",
                details(fromBookId, toBookId, amount));

        throw new IllegalStateException(
                "Intentional outer failure after REQUIRES_NEW audit");
    }

    private void moveStock(
            long fromBookId,
            long toBookId,
            int amount) {
        validateTransfer(fromBookId, toBookId, amount);
        stockService.decrease(fromBookId, amount);
        stockService.increase(toBookId, amount);
    }

    private static void validateTransfer(
            long fromBookId,
            long toBookId,
            int amount) {
        if (fromBookId == toBookId) {
            throw new IllegalArgumentException(
                    "source and destination books must differ");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }

    private static String details(
            long fromBookId,
            long toBookId,
            int amount) {
        return "fromBookId=%d, toBookId=%d, amount=%d"
                .formatted(fromBookId, toBookId, amount);
    }
}
