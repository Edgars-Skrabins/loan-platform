package io.github.edgarsskrabins.loan_platform.profile.service;

import io.github.edgarsskrabins.loan_platform.exceptions.ProfileSettingsNotFoundException;
import io.github.edgarsskrabins.loan_platform.profile.entity.ProfileSettings;
import io.github.edgarsskrabins.loan_platform.profile.entity.Theme;
import io.github.edgarsskrabins.loan_platform.profile.repository.ProfileSettingsRepository;
import io.github.edgarsskrabins.loan_platform.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileSettingsService {

    private final ProfileSettingsRepository profileSettingsRepository;

    @Transactional
    public ProfileSettings create(User user) {
        ProfileSettings profileSettings = new ProfileSettings();
        profileSettings.setUser(user);
        profileSettings.setTheme(Theme.LIGHT);
        return profileSettingsRepository.save(profileSettings);
    }

    @Transactional(readOnly = true)
    public ProfileSettings getByUserId(Long userId) {
        return profileSettingsRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileSettingsNotFoundException(userId));
    }

    @Transactional
    public ProfileSettings updateTheme(Long userId, Theme theme) {
        ProfileSettings profileSettings = getByUserId(userId);
        profileSettings.setTheme(theme);
        return profileSettingsRepository.save(profileSettings);
    }
}
