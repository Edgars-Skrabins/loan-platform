package io.github.edgarsskrabins.loan_platform.profile.dto;

import io.github.edgarsskrabins.loan_platform.profile.entity.Theme;
import jakarta.validation.constraints.NotNull;

public record UpdateProfileSettingsRequest(
        @NotNull(message = "Theme is required")
        Theme theme
) {
}
