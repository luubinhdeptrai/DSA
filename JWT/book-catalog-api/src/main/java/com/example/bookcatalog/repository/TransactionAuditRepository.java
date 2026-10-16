package com.example.bookcatalog.repository;

import com.example.bookcatalog.model.TransactionAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionAuditRepository extends JpaRepository<TransactionAudit, Long> {
    
}
