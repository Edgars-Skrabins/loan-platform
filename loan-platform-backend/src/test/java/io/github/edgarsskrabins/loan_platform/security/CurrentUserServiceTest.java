package io.github.edgarsskrabins.loan_platform.security;

import io.github.edgarsskrabins.loan_platform.user.entity.Role;
import io.github.edgarsskrabins.loan_platform.user.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrentUserServiceTest {

    private final CurrentUserService currentUserService = new CurrentUserService();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("getCurrentUser returns the User principal the JWT filter installs")
    void resolvesAuthenticatedUser() {
        User user = user("ada@example.com");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of())
        );

        assertThat(currentUserService.getCurrentUser()).isSameAs(user);
    }

    @Test
    @DisplayName("getCurrentUser throws NullPointerException on an empty security context")
    void failsOnEmptyContext() {
        assertThatThrownBy(() -> currentUserService.getCurrentUser())
                .isInstanceOf(NullPointerException.class);
    }

    private static User user(String email) {
        User user = new User();
        user.setId(7L);
        user.setEmail(email);
        user.setRole(Role.CUSTOMER);
        return user;
    }
}
