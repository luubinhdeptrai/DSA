package com.example.securitydemo.auth;

import com.example.securitydemo.user.AppUser;
import com.example.securitydemo.user.AppUserRepository;
import com.example.securitydemo.user.Role;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class RegistrationService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String username = request.username().strip().toLowerCase(Locale.ROOT);

        if (appUserRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException();
        }

        String passwordHash = passwordEncoder.encode(request.password());
        AppUser appUser = new AppUser(username, passwordHash, Role.USER);

        try {
            AppUser saved = appUserRepository.saveAndFlush(appUser);
            return new RegisterResponse(saved.getUsername(), saved.getRole().name());
        } catch (DataIntegrityViolationException exception) {
            // The UNIQUE constraint closes the race between existsByUsername and insert.
            throw new UsernameAlreadyExistsException();
        }
    }
}
