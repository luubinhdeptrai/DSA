package com.example.securitydemo.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegistrationService registrationService;

    public AuthController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/csrf")
    public CsrfResponse csrf(CsrfToken csrfToken) {
        return new CsrfResponse(
                csrfToken.getHeaderName(),
                csrfToken.getParameterName(),
                csrfToken.getToken()
        );
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        RegisterResponse response = registrationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public MeResponse me(
            @AuthenticationPrincipal UserDetails principal,
            Authentication authentication
    ) {
        List<String> roleAuthorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .sorted()
                .toList();

        return new MeResponse(principal.getUsername(), roleAuthorities);
    }

    public record CsrfResponse(
            String headerName,
            String parameterName,
            String token
    ) {
    }

    public record MeResponse(
            String username,
            List<String> authorities
    ) {
    }
}
