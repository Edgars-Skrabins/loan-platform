package io.github.edgarsskrabins.loan_platform.security;

import io.github.edgarsskrabins.loan_platform.exceptions.UserNotFoundException;
import io.github.edgarsskrabins.loan_platform.user.entity.Role;
import io.github.edgarsskrabins.loan_platform.user.entity.User;
import io.github.edgarsskrabins.loan_platform.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private CurrentUserService currentUserService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("getCurrentUser resolves the authenticated email through UserService")
    void resolvesAuthenticatedUser() {
        User user = user("ada@example.com");
        authenticateAs("ada@example.com");
        when(userService.getUserByEmail("ada@example.com")).thenReturn(user);

        assertThat(currentUserService.getCurrentUser()).isSameAs(user);
    }

    @Test
    @DisplayName("getCurrentUser surfaces UserNotFoundException when the account is gone")
    void propagatesUnknownUser() {
        authenticateAs("deleted@example.com");
        when(userService.getUserByEmail("deleted@example.com"))
                .thenThrow(new UserNotFoundException("deleted@example.com"));

        assertThatThrownBy(() -> currentUserService.getCurrentUser())
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("getCurrentUser throws NullPointerException on an empty security context")
    void failsOnEmptyContext() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> currentUserService.getCurrentUser())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("getCurrentUser works with the User principal the JWT filter installs")
    void worksWithFilterInstalledPrincipal() {
        User user = user("ada@example.com");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of())
        );
        when(userService.getUserByEmail("ada@example.com")).thenReturn(user);

        assertThat(currentUserService.getCurrentUser()).isSameAs(user);
    }

    private static void authenticateAs(String email) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private static User user(String email) {
        User user = new User();
        user.setId(7L);
        user.setEmail(email);
        user.setRole(Role.CUSTOMER);
        return user;
    }
}
