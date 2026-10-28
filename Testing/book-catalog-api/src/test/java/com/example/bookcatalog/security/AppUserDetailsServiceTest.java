package com.example.bookcatalog.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.bookcatalog.model.AppUser;
import com.example.bookcatalog.repository.AppUserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock
    AppUserRepository users;

    @InjectMocks
    AppUserDetailsService service;

    @Test
    void loadUserByUsername_knownAdmin_mapsStoredDataAndAuthority() {
        when(users.findByUsername("admin"))
                .thenReturn(Optional.of(
                        new AppUser("admin", "stored-hash", "ADMIN")));

        UserDetails details = service.loadUserByUsername("admin");

        assertThat(details.getUsername()).isEqualTo("admin");
        assertThat(details.getPassword()).isEqualTo("stored-hash");
        assertThat(details.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_unknownUser_throwsGenericCredentialError() {
        when(users.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void loadUserByUsername_knownUser_mapsUserAuthority() {
        when(users.findByUsername("reader"))
                .thenReturn(Optional.of(new AppUser("reader", "reader-hash", "USER")));
        UserDetails details = service.loadUserByUsername("reader");
        assertThat(details.getUsername()).isEqualTo("reader");
        assertThat(details.getPassword()).isEqualTo("reader-hash");
        assertThat(details.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }
}
