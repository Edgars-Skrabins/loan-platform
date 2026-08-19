package io.github.edgarsskrabins.loan_platform.validation;

import io.github.edgarsskrabins.loan_platform.auth.dto.login.LoginRequest;
import io.github.edgarsskrabins.loan_platform.auth.dto.register.RegisterRequest;
import io.github.edgarsskrabins.loan_platform.loanApplication.dto.createLoanApplication.CreateLoanApplicationRequest;
import io.github.edgarsskrabins.loan_platform.loanApplication.dto.deleteLoanApplication.DeleteLoanApplicationRequest;
import io.github.edgarsskrabins.loan_platform.loanApplication.dto.updateLoanApplication.UpdateLoanApplicationStatusRequest;
import io.github.edgarsskrabins.loan_platform.loanApplication.entity.LoanStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

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
        @DisplayName("accepts an id plus a target status")
        void acceptsValidInput() {
            assertThat(validator.validate(
                    new UpdateLoanApplicationStatusRequest(1L, LoanStatus.APPROVED))).isEmpty();
        }

        @Test
        @DisplayName("rejects a null id")
        void requiresId() {
            assertThat(messagesFor(validator.validate(
                    new UpdateLoanApplicationStatusRequest(null, LoanStatus.APPROVED))))
                    .contains("Loan application id is required");
        }

        @Test
        @DisplayName("rejects a missing target status rather than writing null to a NOT NULL column")
        void requiresNewStatus() {
            assertThat(messagesFor(validator.validate(
                    new UpdateLoanApplicationStatusRequest(1L, null))))
                    .contains("Target status is required");
        }
    }

    @Nested
    @DisplayName("CreateLoanApplicationRequest")
    class CreateRequest {

        @Test
        @DisplayName("accepts a positive amount and term")
        void acceptsValidInput() {
            assertThat(validator.validate(
                    new CreateLoanApplicationRequest(new BigDecimal("5000.00"), 24))).isEmpty();
        }

        @Test
        @DisplayName("rejects a null amount without blowing up on the BigDecimal type")
        void rejectsNullAmount() {
            assertThat(messagesFor(validator.validate(new CreateLoanApplicationRequest(null, 24))))
                    .contains("Please specify the loan amount");
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "-1", "-5000.00"})
        @DisplayName("rejects a zero or negative amount")
        void rejectsNonPositiveAmount(String amount) {
            assertThat(messagesFor(validator.validate(
                    new CreateLoanApplicationRequest(new BigDecimal(amount), 24))))
                    .contains("Loan amount must be positive");
        }

        @Test
        @DisplayName("rejects a missing term, because term_months is NOT NULL")
        void rejectsNullTerm() {
            assertThat(messagesFor(validator.validate(
                    new CreateLoanApplicationRequest(new BigDecimal("5000.00"), null))))
                    .contains("Please specify the loan term in months");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1})
        @DisplayName("rejects a zero or negative term")
        void rejectsNonPositiveTerm(int termMonths) {
            assertThat(messagesFor(validator.validate(
                    new CreateLoanApplicationRequest(new BigDecimal("5000.00"), termMonths))))
                    .contains("Loan term must be positive");
        }

        @Test
        @DisplayName("rejects an implausibly long term")
        void rejectsExcessiveTerm() {
            assertThat(messagesFor(validator.validate(
                    new CreateLoanApplicationRequest(new BigDecimal("5000.00"), 481))))
                    .contains("Loan term cannot exceed 480 months");
        }
    }

    private static <T> Set<String> messagesFor(Set<ConstraintViolation<T>> violations) {
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }
}
