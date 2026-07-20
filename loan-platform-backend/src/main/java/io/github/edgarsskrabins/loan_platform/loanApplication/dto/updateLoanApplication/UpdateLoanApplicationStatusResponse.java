package io.github.edgarsskrabins.loan_platform.loanApplication.dto.updateLoanApplication;

import io.github.edgarsskrabins.loan_platform.loanApplication.entity.LoanStatus;

public record UpdateLoanApplicationStatusResponse(
        Long id,
        LoanStatus newStatus
) {
}
