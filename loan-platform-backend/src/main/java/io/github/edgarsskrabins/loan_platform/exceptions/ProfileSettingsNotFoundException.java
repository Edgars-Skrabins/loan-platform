package io.github.edgarsskrabins.loan_platform.exceptions;

public class ProfileSettingsNotFoundException extends RuntimeException {

    public ProfileSettingsNotFoundException(Long userId) {
        super("Profile settings not found for user ID: " + userId);
    }
}
