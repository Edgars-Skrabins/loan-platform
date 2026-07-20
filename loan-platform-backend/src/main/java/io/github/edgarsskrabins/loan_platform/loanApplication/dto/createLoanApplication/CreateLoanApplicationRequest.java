package io.github.edgarsskrabins.loan_platform.loanApplication.dto.createLoanApplication;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateLoanApplicationRequest(
        @NotBlank(message = "Please specify the loan amount")
        @Positive
        BigDecimal amount
) {
}
