package com.example.securitydemo.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AdminOperationService {

    private final AdminAuditEventRepository adminAuditEventRepository;

    public AdminOperationService(
            AdminAuditEventRepository adminAuditEventRepository
    ) {
        this.adminAuditEventRepository = adminAuditEventRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public String executeAdminOperation() {
        return "method-security-protected operation executed";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public AdminAuditResponse recordAdminAudit(String username) {
        AdminAuditEvent event = new AdminAuditEvent(
                username,
                "ADMIN_DEMO_ACTION",
                Instant.now()
        );
        AdminAuditEvent saved = adminAuditEventRepository.save(event);

        return new AdminAuditResponse(
                saved.getId(),
                saved.getAction(),
                saved.getCreatedAt()
        );
    }
}
