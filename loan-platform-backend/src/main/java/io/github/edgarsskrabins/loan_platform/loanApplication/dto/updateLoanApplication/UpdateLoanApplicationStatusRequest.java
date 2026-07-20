package io.github.edgarsskrabins.loan_platform.loanApplication.dto.updateLoanApplication;

import io.github.edgarsskrabins.loan_platform.loanApplication.entity.LoanStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateLoanApplicationStatusRequest(
        @NotNull
        Long id,
        LoanStatus newStatus
) {
}
