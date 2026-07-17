-- V1__init.sql | Initial schema for the loan platform.

-- =========================================================
-- users: authentication + identity. One row per login account.
-- =========================================================
CREATE TABLE users
(
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(30)  NOT NULL, -- CUSTOMER | LOAN_OFFICER | ADMIN
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- =========================================================
-- customer_profiles: financial info for a CUSTOMER user.
-- One profile per user (user_id is UNIQUE).
-- debt field intentionally deferred to a later migration.
-- =========================================================
CREATE TABLE customer_profiles
(
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT NOT NULL UNIQUE REFERENCES users (id),
    monthly_income    NUMERIC(12, 2),
    employment_status VARCHAR(50),
    credit_score      INT
);

-- =========================================================
-- loan_applications: a customer's request for a loan.
-- status starts PENDING; officers move it to APPROVED/REJECTED.
-- =========================================================
CREATE TABLE loan_applications
(
    id            BIGSERIAL PRIMARY KEY,
    customer_id   BIGINT         NOT NULL REFERENCES customer_profiles (id),
    amount        NUMERIC(12, 2) NOT NULL,
    term_months   INT            NOT NULL,
    interest_rate NUMERIC(5, 2),
    status        VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ    NOT NULL DEFAULT now()
);

-- =========================================================
-- loan_decisions: append-only history of officer decisions.
-- We never overwrite a loan's status silently — every decision
-- is a permanent record here (who, what, when, why).
-- =========================================================
CREATE TABLE loan_decisions
(
    id         BIGSERIAL PRIMARY KEY,
    loan_id    BIGINT      NOT NULL REFERENCES loan_applications (id),
    officer_id BIGINT      NOT NULL REFERENCES users (id),
    decision   VARCHAR(20) NOT NULL, -- APPROVED | REJECTED
    comment    TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =========================================================
-- audit_logs: track important actions across the platform.
-- user_id is nullable (some actions may be system-initiated).
-- =========================================================
CREATE TABLE audit_logs
(
    id        BIGSERIAL PRIMARY KEY,
    user_id   BIGINT REFERENCES users (id),
    action    VARCHAR(100) NOT NULL, -- USER_LOGIN | LOAN_APPROVED | ...
    timestamp TIMESTAMPTZ  NOT NULL DEFAULT now()
);
