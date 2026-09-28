package com.example.securitydemo.user;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    public DatabaseUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        String normalizedUsername = username.strip().toLowerCase(Locale.ROOT);

        AppUser appUser = appUserRepository.findByUsername(normalizedUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        return User.withUsername(appUser.getUsername())
                .password(appUser.getPasswordHash())
                .roles(appUser.getRole().name())
                .build();
    }
}
