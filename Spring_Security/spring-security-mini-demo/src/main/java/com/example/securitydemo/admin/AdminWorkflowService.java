package com.example.securitydemo.admin;

import org.springframework.stereotype.Service;

@Service
public class AdminWorkflowService {

    private final AdminOperationService adminOperationService;

    public AdminWorkflowService(AdminOperationService adminOperationService) {
        this.adminOperationService = adminOperationService;
    }

    public String runProtectedOperation() {
        // Cross-bean call: it passes through AdminOperationService's security proxy.
        return adminOperationService.executeAdminOperation();
    }
}
