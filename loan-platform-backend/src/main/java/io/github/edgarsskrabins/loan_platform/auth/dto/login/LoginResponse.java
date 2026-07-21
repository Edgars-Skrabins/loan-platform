package io.github.edgarsskrabins.loan_platform.auth.dto.login;
import io.github.edgarsskrabins.loan_platform.user.entity.Role;

public record LoginResponse(
        Long id,
        String token,
        String email,
        Role role
) {
}
