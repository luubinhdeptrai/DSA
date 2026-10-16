package com.example.bookcatalog.security;

import com.example.bookcatalog.model.AppUser;
import com.example.bookcatalog.repository.AppUserRepository;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LocalAdminBootstrap implements ApplicationRunner {
    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final String username;
    private final String password;

    public LocalAdminBootstrap(
            AppUserRepository users,
            PasswordEncoder encoder,
            @Value("${BOOK_ADMIN_USERNAME:}") String username,
            @Value("${BOOK_ADMIN_PASSWORD:}") String password) {
        this.users = users;
        this.encoder = encoder;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (username.isBlank() || password.isBlank()) {
            throw new IllegalStateException(
                    "local profile requires BOOK_ADMIN_USERNAME and BOOK_ADMIN_PASSWORD");
        }
        if (!username.matches("[A-Za-z][A-Za-z0-9._-]{2,79}")) {
            throw new IllegalStateException("local admin username is invalid");
        }
        if (password.length() < 12
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException(
                    "local admin password needs at least 12 characters and at most 72 UTF-8 bytes");
        }

        users.findByUsername(username).ifPresentOrElse(existing -> {
            if (!"ADMIN".equals(existing.getRole())) {
                throw new IllegalStateException(
                        "local admin username is already used by a non-admin account");
            }
            // Idempotent: do not overwrite an existing administrator's hash.
        }, () -> users.save(new AppUser(
                username, encoder.encode(password), "ADMIN")));
    }
}
