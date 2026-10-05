package io.github.edgarsskrabins.loan_platform.loanApplication.dto.getLoanApplication;

import io.github.edgarsskrabins.loan_platform.loanApplication.entity.LoanStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record GetLoanApplicationResponse(
        Long id,
        Long customerId,
        BigDecimal amount,
        Integer termMonths,
        BigDecimal interestRate,
        LoanStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
