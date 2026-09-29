package com.example.bookcatalog.service;

import com.example.bookcatalog.dto.RegisterRequest;
import com.example.bookcatalog.model.AppUser;
import com.example.bookcatalog.repository.AppUserRepository;
import java.nio.charset.StandardCharsets;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RegistrationService {
    private final AppUserRepository users;
    private final PasswordEncoder encoder;

    public RegistrationService(AppUserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password exceeds the 72-byte BCrypt limit");
        }
        if (users.existsByUsername(request.username())) {
            throw duplicateUsername();
        }

        try {
            users.saveAndFlush(new AppUser(
                    request.username(), encoder.encode(request.password()), "USER"));
        } catch (DataIntegrityViolationException duplicateOrConstraint) {
            // The unique database constraint also protects concurrent registrations.
            throw duplicateUsername();
        }
    }

    private static ResponseStatusException duplicateUsername() {
        return new ResponseStatusException(HttpStatus.CONFLICT,
                "Username is unavailable");
    }
}
