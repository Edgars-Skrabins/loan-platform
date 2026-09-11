package io.github.edgarsskrabins.loan_platform.profile.repository;

import io.github.edgarsskrabins.loan_platform.profile.entity.ProfileSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileSettingsRepository extends JpaRepository<ProfileSettings, Long> {

    Optional<ProfileSettings> findByUserId(Long userId);

}
