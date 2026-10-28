package com.example.bookcatalog.service;

import com.example.bookcatalog.model.TransactionAudit;
import com.example.bookcatalog.repository.TransactionAuditRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionAuditService {

    private final TransactionAuditRepository auditRepository;

    public TransactionAuditService(
            TransactionAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void recordRequired(String eventType, String details) {
        auditRepository.save(new TransactionAudit(
                eventType,
                details,
                Instant.now()));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void recordRequiredThenFail(String eventType, String details) {
        auditRepository.saveAndFlush(new TransactionAudit(
                eventType,
                details,
                Instant.now()));

        throw new IllegalStateException(
                "Intentional inner REQUIRED failure");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordRequiresNew(String eventType, String details) {
        auditRepository.saveAndFlush(new TransactionAudit(
                eventType,
                details,
                Instant.now()));
    }
}
