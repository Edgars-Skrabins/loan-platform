package io.github.edgarsskrabins.loan_platform.profile.dto;

import io.github.edgarsskrabins.loan_platform.profile.entity.Theme;

public record GetProfileSettingsResponse(
        Long userId,
        Theme theme
) {
}
