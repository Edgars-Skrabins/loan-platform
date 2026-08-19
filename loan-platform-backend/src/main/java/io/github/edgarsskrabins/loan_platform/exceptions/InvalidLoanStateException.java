package io.github.edgarsskrabins.loan_platform.exceptions;

public class InvalidLoanStateException extends RuntimeException {

    public InvalidLoanStateException(String message) {
        super(message);
    }
}
