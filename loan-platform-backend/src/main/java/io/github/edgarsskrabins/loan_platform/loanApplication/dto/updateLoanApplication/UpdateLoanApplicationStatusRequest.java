package io.github.edgarsskrabins.loan_platform.loanApplication.dto.updateLoanApplication;

import io.github.edgarsskrabins.loan_platform.loanApplication.entity.LoanStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateLoanApplicationStatusRequest(
        @NotNull(message = "Loan application id is required")
        Long id,

        @NotNull(message = "Target status is required")
        LoanStatus newStatus
) {
}
