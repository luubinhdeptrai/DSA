package com.example.bookcatalog.web;

import com.example.bookcatalog.dto.AccessTokenResponse;
import com.example.bookcatalog.dto.CurrentUserResponse;
import com.example.bookcatalog.dto.LoginRequest;
import com.example.bookcatalog.dto.RegisterRequest;
import com.example.bookcatalog.security.TokenService;
import com.example.bookcatalog.service.RegistrationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final RegistrationService registration;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokens;

    public AuthController(
            RegistrationService registration,
            AuthenticationManager authenticationManager,
            TokenService tokens) {
        this.registration = registration;
        this.authenticationManager = authenticationManager;
        this.tokens = tokens;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterRequest request) {
        registration.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponse> login(
            @Valid @RequestBody LoginRequest request) {
        Authentication attempt = UsernamePasswordAuthenticationToken
                .unauthenticated(request.username(), request.password());
        Authentication verified = authenticationManager.authenticate(attempt);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(tokens.issue(verified));
    }

    @GetMapping("/me")
    public CurrentUserResponse me(Authentication authentication) {
        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return new CurrentUserResponse(authentication.getName(), authorities);
    }
}
