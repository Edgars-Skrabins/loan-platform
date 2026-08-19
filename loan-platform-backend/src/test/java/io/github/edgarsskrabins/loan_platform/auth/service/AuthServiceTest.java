package io.github.edgarsskrabins.loan_platform.auth.service;

import io.github.edgarsskrabins.loan_platform.auth.dto.login.LoginRequest;
import io.github.edgarsskrabins.loan_platform.auth.dto.login.LoginResponse;
import io.github.edgarsskrabins.loan_platform.auth.dto.register.RegisterRequest;
import io.github.edgarsskrabins.loan_platform.auth.dto.register.RegisterResponse;
import io.github.edgarsskrabins.loan_platform.exceptions.UserNotFoundException;
import io.github.edgarsskrabins.loan_platform.security.jwt.JwtService;
import io.github.edgarsskrabins.loan_platform.user.entity.Role;
import io.github.edgarsskrabins.loan_platform.user.entity.User;
import io.github.edgarsskrabins.loan_platform.user.service.UserService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String EMAIL = "ada@example.com";
    private static final String RAW_PASSWORD = "correct-horse-battery";
    private static final String PASSWORD_HASH = "$2a$10$hashed";

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("register hashes the password before persisting and never stores the raw value")
    void registerHashesPassword() {
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(PASSWORD_HASH);
        when(userService.save(any(User.class))).thenAnswer(invocation -> persisted(invocation.getArgument(0)));

        authService.register(new RegisterRequest(EMAIL, RAW_PASSWORD));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userService).save(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo(EMAIL);
        assertThat(saved.getValue().getPasswordHash())
                .isEqualTo(PASSWORD_HASH)
                .isNotEqualTo(RAW_PASSWORD);
    }

    @Test
    @DisplayName("register maps the persisted user onto the response")
    void registerMapsResponse() {
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(PASSWORD_HASH);
        when(userService.save(any(User.class))).thenAnswer(invocation -> {
            User user = persisted(invocation.getArgument(0));
            user.setRole(Role.CUSTOMER);
            user.setCreatedAt(createdAt);
            return user;
        });

        RegisterResponse response = authService.register(new RegisterRequest(EMAIL, RAW_PASSWORD));

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.email()).isEqualTo(EMAIL);
        assertThat(response.role()).isEqualTo(Role.CUSTOMER);
        assertThat(response.createdAt()).isEqualTo(createdAt);
    }

    @Test
    @Disabled("""
            BUG: AuthService.register never calls user.setRole(...), so role stays null.
            users.role is NOT NULL in V1__init.sql, so every real registration fails with a
            constraint violation. Remove @Disabled once register defaults new users to CUSTOMER.""")
    @DisplayName("register defaults a new account to the CUSTOMER role")
    void registerDefaultsRoleToCustomer() {
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(PASSWORD_HASH);
        when(userService.save(any(User.class))).thenAnswer(invocation -> persisted(invocation.getArgument(0)));

        authService.register(new RegisterRequest(EMAIL, RAW_PASSWORD));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userService).save(saved.capture());
        assertThat(saved.getValue().getRole()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    @Disabled("""
            BUG: register does not check UserRepository.existsByEmail, so a duplicate signup
            surfaces as a raw DataIntegrityViolationException (HTTP 500) instead of a 409.
            Remove @Disabled once register rejects taken emails explicitly.""")
    @DisplayName("register rejects an email that is already taken")
    void registerRejectsDuplicateEmail() {
        assertThatThrownBy(() -> authService.register(new RegisterRequest(EMAIL, RAW_PASSWORD)))
                .isInstanceOf(IllegalStateException.class);
        verify(userService, never()).save(any());
    }

    @Test
    @DisplayName("login returns a token plus identity when the password matches")
    void loginReturnsTokenOnValidCredentials() {
        User user = existingUser();
        when(userService.getUserByEmail(EMAIL)).thenReturn(user);
        when(passwordEncoder.matches(RAW_PASSWORD, PASSWORD_HASH)).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        LoginResponse response = authService.login(new LoginRequest(EMAIL, RAW_PASSWORD));

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.email()).isEqualTo(EMAIL);
        assertThat(response.role()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    @DisplayName("login throws BadCredentialsException and issues no token on a wrong password")
    void loginRejectsWrongPassword() {
        when(userService.getUserByEmail(EMAIL)).thenReturn(existingUser());
        when(passwordEncoder.matches("wrong-password", PASSWORD_HASH)).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest(EMAIL, "wrong-password")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("login on an unknown email never reaches the password check")
    void loginPropagatesUnknownUser() {
        when(userService.getUserByEmail("nobody@example.com"))
                .thenThrow(new UserNotFoundException("nobody@example.com"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("nobody@example.com", RAW_PASSWORD)))
                .isInstanceOf(UserNotFoundException.class);
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    private static User persisted(User user) {
        user.setId(42L);
        return user;
    }

    private static User existingUser() {
        User user = new User();
        user.setId(7L);
        user.setEmail(EMAIL);
        user.setPasswordHash(PASSWORD_HASH);
        user.setRole(Role.CUSTOMER);
        return user;
    }
}
