package io.github.edgarsskrabins.loan_platform.exceptions;

public class LoanApplicationNotFoundException extends RuntimeException {

    public LoanApplicationNotFoundException(Long id) {
        super("Loan application not found: " + id);
    }
}
