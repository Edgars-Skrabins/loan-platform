package io.github.edgarsskrabins.loan_platform.audit.entity;

public final class AuditAction {
    public static final String USER_LOGIN     = "USER_LOGIN";
    public static final String USER_REGISTERED = "USER_REGISTERED";
    public static final String LOAN_APPROVED  = "LOAN_APPROVED";
    public static final String LOAN_REJECTED  = "LOAN_REJECTED";

    private AuditAction() {}
}
