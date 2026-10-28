package com.example.bookcatalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.bookcatalog.dto.RegisterRequest;
import com.example.bookcatalog.model.AppUser;
import com.example.bookcatalog.repository.AppUserRepository;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    private static final String RAW_PASSWORD = "test-password-123";

    @Mock
    AppUserRepository users;

    @Mock
    PasswordEncoder encoder;

    @InjectMocks
    RegistrationService service;

    @Test
    void register_newUsername_encodesPasswordAndAssignsUserRole() {
        RegisterRequest request = new RegisterRequest("alice", RAW_PASSWORD);
        when(users.existsByUsername("alice")).thenReturn(false);
        when(encoder.encode(RAW_PASSWORD)).thenReturn("encoded-test-value");

        service.register(request);

        ArgumentCaptor<AppUser> saved = ArgumentCaptor.forClass(AppUser.class);
        verify(users).saveAndFlush(saved.capture());
        verify(encoder).encode(RAW_PASSWORD);
        assertThat(saved.getValue().getUsername()).isEqualTo("alice");
        assertThat(saved.getValue().getPasswordHash())
                .isEqualTo("encoded-test-value")
                .isNotEqualTo(RAW_PASSWORD);
        assertThat(saved.getValue().getRole()).isEqualTo("USER");
    }

    @Test
    void register_existingUsername_returnsConflictWithoutEncodingOrSaving() {
        RegisterRequest request = new RegisterRequest("alice", RAW_PASSWORD);
        when(users.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode())
                                .isEqualTo(HttpStatus.CONFLICT));

        verifyNoInteractions(encoder);
        verify(users, never()).saveAndFlush(any(AppUser.class));
    }

    @Test
    void register_concurrentDuplicate_translatesDatabaseFailureToConflict() {
        RegisterRequest request = new RegisterRequest("alice", RAW_PASSWORD);
        when(users.existsByUsername("alice")).thenReturn(false);
        when(encoder.encode(RAW_PASSWORD)).thenReturn("encoded-test-value");
        when(users.saveAndFlush(any(AppUser.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode())
                                .isEqualTo(HttpStatus.CONFLICT));
    }

    static Stream<String> passwordsAtByteLimit() {
        return Stream.of("a".repeat(72), "é".repeat(36));
    }

    @ParameterizedTest(name = "accepted byte-limit case {index}")
    @MethodSource("passwordsAtByteLimit")
    void register_passwordAt72Bytes_reachesEncoderAndSave(String password) {
        when(users.existsByUsername("boundary-user")).thenReturn(false);
        when(encoder.encode(password)).thenReturn("encoded-boundary");

        service.register(new RegisterRequest("boundary-user", password));

        verify(encoder).encode(password);
        ArgumentCaptor<AppUser> saved = ArgumentCaptor.forClass(AppUser.class);
        verify(users).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("encoded-boundary");
        assertThat(saved.getValue().getRole()).isEqualTo("USER");
    }

    static Stream<String> passwordsOverByteLimit() {
        return Stream.of("a".repeat(73), "é".repeat(37));
    }

    @ParameterizedTest(name = "rejected byte-limit case {index}")
    @MethodSource("passwordsOverByteLimit")
    void register_passwordOver72Bytes_rejectsBeforeAnyCollaborator(String password) {
        assertThatThrownBy(() -> service.register(new RegisterRequest("boundary-user", password)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> {
                            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                            assertThat(exception.getReason())
                                    .isEqualTo("Password exceeds the 72-byte BCrypt limit");
                        });
        verifyNoInteractions(users, encoder);
    }
}
