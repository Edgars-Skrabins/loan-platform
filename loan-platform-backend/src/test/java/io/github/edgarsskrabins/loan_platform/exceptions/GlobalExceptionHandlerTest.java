package io.github.edgarsskrabins.loan_platform.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("a missing user is 404, not 500")
    void userNotFoundIsNotFound() {
        ResponseEntity<ApiError> response = handler.handleNotFound(new UserNotFoundException("ada@example.com"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().message()).contains("ada@example.com");
    }

    @Test
    @DisplayName("a missing loan application is 404")
    void loanApplicationNotFoundIsNotFound() {
        assertThat(handler.handleNotFound(new LoanApplicationNotFoundException(100L)).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("a missing customer profile is 404")
    void customerProfileNotFoundIsNotFound() {
        assertThat(handler.handleNotFound(new CustomerProfileNotFoundException(7L)).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("a duplicate signup is 409, not a raw constraint-violation 500")
    void duplicateEmailIsConflict() {
        ResponseEntity<ApiError> response =
                handler.handleEmailAlreadyInUse(new EmailAlreadyInUseException("ada@example.com"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("acting on a loan in the wrong state is 409")
    void invalidStateIsConflict() {
        ResponseEntity<ApiError> response =
                handler.handleInvalidState(new InvalidLoanStateException("Only pending loan applications can be deleted"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Only pending loan applications can be deleted");
    }

    @Test
    @DisplayName("a role or ownership violation is 403")
    void forbiddenOperationIsForbidden() {
        ResponseEntity<ApiError> response =
                handler.handleForbidden(new ForbiddenOperationException("nope"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("bad credentials are 401 and the message never says which field was wrong")
    void badCredentialsIsUnauthorized() {
        ResponseEntity<ApiError> response =
                handler.handleBadCredentials(new BadCredentialsException("user ada@example.com has a bad password"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Invalid credentials");
        assertThat(response.getBody().message()).doesNotContain("ada@example.com");
    }

    @Test
    @DisplayName("validation failures are 400 with a field-keyed map of messages")
    void validationFailureIsBadRequestWithFieldErrors() throws Exception {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "createLoanApplicationRequest");
        bindingResult.rejectValue(null, "NotNull", "Please specify the loan amount");
        bindingResult.addError(new org.springframework.validation.FieldError(
                "createLoanApplicationRequest", "amount", "Please specify the loan amount"));
        bindingResult.addError(new org.springframework.validation.FieldError(
                "createLoanApplicationRequest", "termMonths", "Please specify the loan term in months"));

        ResponseEntity<ApiError> response = handler.handleValidation(
                new MethodArgumentNotValidException(null, bindingResult));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().fieldErrors())
                .containsEntry("amount", "Please specify the loan amount")
                .containsEntry("termMonths", "Please specify the loan term in months");
    }

    @Test
    @DisplayName("an unexpected exception is 500 with a generic body that leaks nothing")
    void unexpectedExceptionIsGeneric() {
        ResponseEntity<ApiError> response =
                handler.handleUnexpected(new IllegalStateException("jdbc:postgresql://localhost:5432 connection refused"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Something went wrong");
        assertThat(response.getBody().message()).doesNotContain("jdbc");
    }
}
