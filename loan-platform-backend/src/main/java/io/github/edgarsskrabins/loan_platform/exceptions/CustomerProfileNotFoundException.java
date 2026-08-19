package io.github.edgarsskrabins.loan_platform.exceptions;

public class CustomerProfileNotFoundException extends RuntimeException {

    public CustomerProfileNotFoundException(Long userId) {
        super("No customer profile for user: " + userId);
    }
}
