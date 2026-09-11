-- =========================================================
-- profile_settings: track profile settings such as dark mode and other web-based user preferences.
-- =========================================================

CREATE TABLE profile_settings
(
    id      BIGSERIAL PRIMARY KEY,
    user_id BIGINT     NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    theme   VARCHAR(24) NOT NULL DEFAULT 'LIGHT'
        CHECK (theme IN ('LIGHT', 'DARK'))
);