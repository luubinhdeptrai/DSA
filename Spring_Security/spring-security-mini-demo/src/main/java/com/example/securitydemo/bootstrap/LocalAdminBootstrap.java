package com.example.securitydemo.bootstrap;

import com.example.securitydemo.user.AppUser;
import com.example.securitydemo.user.AppUserRepository;
import com.example.securitydemo.user.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Optional;

@Component
@Profile("local")
public class LocalAdminBootstrap implements ApplicationRunner {

    private static final Logger log =
            LoggerFactory.getLogger(LocalAdminBootstrap.class);

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public LocalAdminBootstrap(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${LOCAL_ADMIN_USERNAME:}") String adminUsername,
            @Value("${LOCAL_ADMIN_PASSWORD:}") String adminPassword
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(adminUsername)
                || !StringUtils.hasText(adminPassword)) {
            log.info("Local admin provisioning skipped: credentials are not set");
            return;
        }

        String username = adminUsername.strip().toLowerCase(Locale.ROOT);
        Optional<AppUser> existing = appUserRepository.findByUsername(username);

        if (existing.isPresent()) {
            if (existing.get().getRole() == Role.ADMIN) {
                log.info("Local admin account already exists; leaving it unchanged");
            } else {
                log.warn("Local admin username belongs to a USER; not promoting it");
            }
            return;
        }

        String passwordHash = passwordEncoder.encode(adminPassword);
        appUserRepository.saveAndFlush(
                new AppUser(username, passwordHash, Role.ADMIN)
        );
        log.info("Created the disposable local admin account '{}'; password not logged",
                username);
    }
}
