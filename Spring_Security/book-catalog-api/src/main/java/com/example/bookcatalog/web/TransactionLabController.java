package com.example.bookcatalog.web;

import com.example.bookcatalog.dto.StockTransferRequest;
import com.example.bookcatalog.exception.TransactionLabCheckedException;
import com.example.bookcatalog.service.StockTransferService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transaction-lab")
public class TransactionLabController {

    private final StockTransferService stockTransferService;

    public TransactionLabController(
            StockTransferService stockTransferService) {
        this.stockTransferService = stockTransferService;
    }

    @PostMapping(
            path = "/transfers",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> runTransferScenario(
            @Valid @RequestBody StockTransferRequest request)
            throws TransactionLabCheckedException {
        long fromBookId = request.fromBookId();
        long toBookId = request.toBookId();
        int amount = request.amount();

        switch (request.scenario()) {
            case SUCCESS_REQUIRED ->
                    stockTransferService.transferSuccessfully(
                            fromBookId, toBookId, amount);
            case RUNTIME_ROLLBACK ->
                    stockTransferService.transferThenRuntimeRollback(
                            fromBookId, toBookId, amount);
            case FLUSH_THEN_RUNTIME_ROLLBACK ->
                    stockTransferService.flushThenRuntimeRollback(
                            fromBookId, toBookId, amount);
            case CHECKED_DEFAULT_COMMIT ->
                    stockTransferService.checkedFailureDefault(
                            fromBookId, toBookId, amount);
            case CHECKED_ROLLBACK ->
                    stockTransferService.checkedFailureWithRollback(
                            fromBookId, toBookId, amount);
            case CAUGHT_REQUIRED_INNER_FAILURE ->
                    stockTransferService.catchInnerRequiredFailure(
                            fromBookId, toBookId, amount);
            case REQUIRES_NEW_AUDIT ->
                    stockTransferService.requiresNewAuditThenFail(
                            fromBookId, toBookId, amount);
        }

        return ResponseEntity.noContent().build();
    }
}
