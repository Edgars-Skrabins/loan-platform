package io.github.edgarsskrabins.loan_platform.profile.controller;

import io.github.edgarsskrabins.loan_platform.profile.dto.GetProfileSettingsResponse;
import io.github.edgarsskrabins.loan_platform.profile.dto.UpdateProfileSettingsRequest;
import io.github.edgarsskrabins.loan_platform.profile.entity.ProfileSettings;
import io.github.edgarsskrabins.loan_platform.profile.service.ProfileSettingsService;
import io.github.edgarsskrabins.loan_platform.security.CurrentUserService;
import io.github.edgarsskrabins.loan_platform.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile/settings")
@RequiredArgsConstructor
public class ProfileSettingsController {

    private final ProfileSettingsService profileSettingsService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public GetProfileSettingsResponse getProfileSettings() {
        User currentUser = currentUserService.getCurrentUser();
        ProfileSettings profileSettings = profileSettingsService.getByUserId(currentUser.getId());

        return new GetProfileSettingsResponse(
                currentUser.getId(),
                profileSettings.getTheme()
        );
    }

    @PutMapping
    public GetProfileSettingsResponse updateProfileSettings(
            @Valid @RequestBody UpdateProfileSettingsRequest request
    ) {
        User currentUser = currentUserService.getCurrentUser();
        ProfileSettings profileSettings = profileSettingsService.updateTheme(
                currentUser.getId(),
                request.theme()
        );

        return new GetProfileSettingsResponse(
                currentUser.getId(),
                profileSettings.getTheme()
        );
    }
}
