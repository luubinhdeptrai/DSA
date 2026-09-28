package com.example.securitydemo.admin;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminWorkflowService adminWorkflowService;
    private final AdminOperationService adminOperationService;

    public AdminController(
            AdminWorkflowService adminWorkflowService,
            AdminOperationService adminOperationService
    ) {
        this.adminWorkflowService = adminWorkflowService;
        this.adminOperationService = adminOperationService;
    }

    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("message", "hello from an ADMIN-only endpoint");
    }

    @PostMapping("/action")
    public Map<String, String> action() {
        return Map.of("message", adminWorkflowService.runProtectedOperation());
    }

    @PostMapping("/audit")
    public AdminAuditResponse audit(
            @AuthenticationPrincipal UserDetails principal
    ) {
        return adminOperationService.recordAdminAudit(principal.getUsername());
    }
}
