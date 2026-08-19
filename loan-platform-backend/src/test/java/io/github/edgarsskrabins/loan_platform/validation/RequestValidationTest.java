package io.github.edgarsskrabins.loan_platform.validation;

import io.github.edgarsskrabins.loan_platform.auth.dto.login.LoginRequest;
import io.github.edgarsskrabins.loan_platform.auth.dto.register.RegisterRequest;
import io.github.edgarsskrabins.loan_platform.loanApplication.dto.createLoanApplication.CreateLoanApplicationRequest;
import io.github.edgarsskrabins.loan_platform.loanApplication.dto.deleteLoanApplication.DeleteLoanApplicationRequest;
import io.github.edgarsskrabins.loan_platform.loanApplication.dto.updateLoanApplication.UpdateLoanApplicationStatusRequest;
import io.github.edgarsskrabins.loan_platform.loanApplication.entity.LoanStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.UnexpectedTypeException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Bean-validation rules on the request DTOs. These are the contract the controllers rely on
 * via {@code @Valid}, so they are worth pinning down without booting Spring.
 */
class RequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDownValidator() {
        factory.close();
    }

    @Nested
    @DisplayName("RegisterRequest")
    class Register {

        @Test
        @DisplayName("accepts a valid email and an 8+ character password")
        void acceptsValidInput() {
            assertThat(validator.validate(new RegisterRequest("ada@example.com", "longenough"))).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "not-an-email", "ada@", "@example.com"})
        @DisplayName("rejects a malformed or blank email")
        void rejectsBadEmail(String email) {
            assertThat(messagesFor(validator.validate(new RegisterRequest(email, "longenough"))))
                    .isNotEmpty();
        }

        @Test
        @DisplayName("rejects a password shorter than 8 characters")
        void rejectsShortPassword() {
            Set<ConstraintViolation<RegisterRequest>> violations =
                    validator.validate(new RegisterRequest("ada@example.com", "short"));

            assertThat(messagesFor(violations)).contains("Password must be at least 8 characters");
        }

        @Test
        @DisplayName("rejects a null password")
        void rejectsNullPassword() {
            Set<ConstraintViolation<RegisterRequest>> violations =
                    validator.validate(new RegisterRequest("ada@example.com", null));

            assertThat(messagesFor(violations)).contains("Password is required");
        }
    }

    @Nested
    @DisplayName("LoginRequest")
    class Login {

        @Test
        @DisplayName("accepts a valid email and any non-blank password")
        void acceptsValidInput() {
            assertThat(validator.validate(new LoginRequest("ada@example.com", "x"))).isEmpty();
        }

        @Test
        @DisplayName("rejects a blank email and a blank password together")
        void rejectsBlankFields() {
            assertThat(messagesFor(validator.validate(new LoginRequest("", ""))))
                    .contains("Email is required", "Password is required");
        }
    }

    @Nested
    @DisplayName("DeleteLoanApplicationRequest")
    class DeleteRequest {

        @Test
        @DisplayName("accepts an id and rejects a null one")
        void requiresId() {
            assertThat(validator.validate(new DeleteLoanApplicationRequest(1L))).isEmpty();
            assertThat(validator.validate(new DeleteLoanApplicationRequest(null))).hasSize(1);
        }
    }

    @Nested
    @DisplayName("UpdateLoanApplicationStatusRequest")
    class UpdateRequest {

        @Test
        @DisplayName("accepts an id plus a target status and rejects a null id")
        void requiresId() {
            assertThat(validator.validate(
                    new UpdateLoanApplicationStatusRequest(1L, LoanStatus.APPROVED))).isEmpty();
            assertThat(validator.validate(
                    new UpdateLoanApplicationStatusRequest(null, LoanStatus.APPROVED))).hasSize(1);
        }

        @Test
        @Disabled("""
                BUG: newStatus carries no @NotNull, so {"id": 1} validates cleanly and the service
                writes a null status into a NOT NULL column. Remove @Disabled once newStatus is
                annotated @NotNull.""")
        @DisplayName("rejects a missing target status")
        void requiresNewStatus() {
            assertThat(validator.validate(new UpdateLoanApplicationStatusRequest(1L, null))).hasSize(1);
        }
    }

    @Nested
    @DisplayName("CreateLoanApplicationRequest")
    class CreateRequest {

        @Test
        @DisplayName("@NotBlank on a BigDecimal blows up the validator instead of validating")
        void notBlankOnBigDecimalIsUnsupported() {
            // Documents the bug below: @NotBlank only supports CharSequence, so Hibernate
            // Validator cannot resolve a validator for BigDecimal and every request to
            // POST /api/loans/loan-application fails with HTTP 500 before the service runs.
            assertThatThrownBy(() ->
                    validator.validate(new CreateLoanApplicationRequest(new BigDecimal("5000.00"))))
                    .isInstanceOf(UnexpectedTypeException.class);
        }

        @Test
        @Disabled("""
                BUG: amount is annotated @NotBlank, which is CharSequence-only. It must be @NotNull.
                Remove @Disabled once the annotation is corrected; then a positive amount validates
                and null / zero / negative amounts produce violations.""")
        @DisplayName("accepts a positive amount and rejects null, zero and negative amounts")
        void validatesAmount() {
            assertThat(validator.validate(new CreateLoanApplicationRequest(new BigDecimal("5000.00")))).isEmpty();
            assertThat(validator.validate(new CreateLoanApplicationRequest(null))).hasSize(1);
            assertThat(validator.validate(new CreateLoanApplicationRequest(BigDecimal.ZERO))).hasSize(1);
            assertThat(validator.validate(new CreateLoanApplicationRequest(new BigDecimal("-1")))).hasSize(1);
        }
    }

    private static <T> Set<String> messagesFor(Set<ConstraintViolation<T>> violations) {
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(java.util.stream.Collectors.toSet());
    }
}
