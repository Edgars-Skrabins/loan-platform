package io.github.edgarsskrabins.loan_platform.auth.dto.register;

import io.github.edgarsskrabins.loan_platform.user.entity.Role;

import java.time.Instant;

public record RegisterResponse(
        Long id,
        String email,
        Role role,
        Instant createdAt
) {
}
