package io.github.edgarsskrabins.loan_platform.loanApplication.dto.createLoanApplication;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateLoanApplicationRequest(
        @NotNull(message = "Please specify the loan amount")
        @Positive(message = "Loan amount must be positive")
        BigDecimal amount,

        @NotNull(message = "Please specify the loan term in months")
        @Positive(message = "Loan term must be positive")
        @Max(value = 480, message = "Loan term cannot exceed 480 months")
        Integer termMonths
) {
}
