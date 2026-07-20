package io.github.edgarsskrabins.loan_platform.loanApplication.dto.createLoanApplication;

import io.github.edgarsskrabins.loan_platform.loanApplication.entity.LoanStatus;

public record CreateLoanApplicationResponse(
        Long id,
        LoanStatus status
) {
}
