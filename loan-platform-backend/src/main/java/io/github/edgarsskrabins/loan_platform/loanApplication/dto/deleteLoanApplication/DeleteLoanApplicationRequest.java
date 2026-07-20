package io.github.edgarsskrabins.loan_platform.loanApplication.dto.deleteLoanApplication;

import jakarta.validation.constraints.NotNull;

public record DeleteLoanApplicationRequest(
        @NotNull
        Long id
) {
}
