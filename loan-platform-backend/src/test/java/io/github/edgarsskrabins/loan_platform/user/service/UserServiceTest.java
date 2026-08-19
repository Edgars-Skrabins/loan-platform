package io.github.edgarsskrabins.loan_platform.user.service;

import io.github.edgarsskrabins.loan_platform.exceptions.UserNotFoundException;
import io.github.edgarsskrabins.loan_platform.user.entity.Role;
import io.github.edgarsskrabins.loan_platform.user.entity.User;
import io.github.edgarsskrabins.loan_platform.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("getUserByEmail returns the user when the repository finds one")
    void getUserByEmailReturnsUser() {
        User user = user("ada@example.com");
        when(userRepository.findByEmail("ada@example.com")).thenReturn(Optional.of(user));

        assertThat(userService.getUserByEmail("ada@example.com")).isSameAs(user);
    }

    @Test
    @DisplayName("getUserByEmail throws UserNotFoundException naming the email")
    void getUserByEmailThrowsWhenMissing() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByEmail("nobody@example.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found: nobody@example.com");
    }

    @Test
    @DisplayName("save delegates to the repository and returns the persisted user")
    void saveDelegatesToRepository() {
        User user = user("ada@example.com");
        User persisted = user("ada@example.com");
        persisted.setId(1L);
        when(userRepository.save(user)).thenReturn(persisted);

        assertThat(userService.save(user)).isSameAs(persisted);
        verify(userRepository).save(user);
    }

    private static User user(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hashed");
        user.setRole(Role.CUSTOMER);
        return user;
    }
}
