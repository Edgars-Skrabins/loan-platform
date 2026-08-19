package io.github.edgarsskrabins.loan_platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.edgarsskrabins.loan_platform.loanApplication.entity.LoanStatus;
import io.github.edgarsskrabins.loan_platform.user.entity.Role;
import io.github.edgarsskrabins.loan_platform.user.entity.User;
import io.github.edgarsskrabins.loan_platform.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("the full happy path: register, log in, apply for a loan, approve it")
    void fullHappyPath() throws Exception {
        register("ada@example.com", "longenough")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ada@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        String customerToken = tokenFor("ada@example.com", "longenough");

        MvcResult created = mockMvc.perform(post("/api/loans/loan-application")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 5000.00, "termMonths": 24}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        long loanId = json(created).get("id").asLong();

        String officerToken = staffTokenFor("officer@example.com", Role.LOAN_OFFICER);

        mockMvc.perform(put("/api/loans/loan-application")
                        .header("Authorization", "Bearer " + officerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new StatusChange(loanId, LoanStatus.APPROVED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(loanId))
                .andExpect(jsonPath("$.newStatus").value("APPROVED"));
    }

    @Test
    @DisplayName("registering the same email twice is 409, not a 500")
    void duplicateRegistrationIsConflict() throws Exception {
        register("ada@example.com", "longenough").andExpect(status().isOk());

        register("ada@example.com", "longenough")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("a bad password is 401 and the body does not say which field was wrong")
    void wrongPasswordIsUnauthorized() throws Exception {
        register("ada@example.com", "longenough").andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "ada@example.com", "password": "wrongpassword"}"""))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    @DisplayName("invalid registration input is 400 with per-field messages")
    void invalidRegistrationIsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "not-an-email", "password": "short"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").isNotEmpty())
                .andExpect(jsonPath("$.fieldErrors.password").isNotEmpty());
    }

    @Test
    @DisplayName("a loan request with no amount or term is 400 with per-field messages")
    void invalidLoanRequestIsBadRequest() throws Exception {
        register("ada@example.com", "longenough").andExpect(status().isOk());
        String token = tokenFor("ada@example.com", "longenough");

        mockMvc.perform(post("/api/loans/loan-application")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.amount").value("Please specify the loan amount"))
                .andExpect(jsonPath("$.fieldErrors.termMonths").value("Please specify the loan term in months"));
    }

    @Test
    @DisplayName("an unauthenticated loan request is 401")
    void unauthenticatedRequestIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/loans/loan-application")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 5000.00, "termMonths": 24}"""))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("a garbage Bearer token is 401, not 500")
    void malformedTokenIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/loans/loan-application")
                        .header("Authorization", "Bearer not-a-real-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 5000.00, "termMonths": 24}"""))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("a customer cannot approve their own loan")
    void customerCannotApproveOwnLoan() throws Exception {
        register("ada@example.com", "longenough").andExpect(status().isOk());
        String token = tokenFor("ada@example.com", "longenough");
        long loanId = createLoan(token);

        mockMvc.perform(put("/api/loans/loan-application")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new StatusChange(loanId, LoanStatus.APPROVED))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("a customer cannot delete another customer's application")
    void customerCannotDeleteAnotherCustomersApplication() throws Exception {
        register("ada@example.com", "longenough").andExpect(status().isOk());
        register("grace@example.com", "longenough").andExpect(status().isOk());

        long adaLoanId = createLoan(tokenFor("ada@example.com", "longenough"));
        String graceToken = tokenFor("grace@example.com", "longenough");

        mockMvc.perform(delete("/api/loans/loan-application")
                        .header("Authorization", "Bearer " + graceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Identifier(adaLoanId))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("a customer can delete their own pending application")
    void customerCanDeleteOwnPendingApplication() throws Exception {
        register("ada@example.com", "longenough").andExpect(status().isOk());
        String token = tokenFor("ada@example.com", "longenough");
        long loanId = createLoan(token);

        mockMvc.perform(delete("/api/loans/loan-application")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Identifier(loanId))))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("an approved application can no longer be deleted")
    void approvedApplicationCannotBeDeleted() throws Exception {
        register("ada@example.com", "longenough").andExpect(status().isOk());
        String customerToken = tokenFor("ada@example.com", "longenough");
        long loanId = createLoan(customerToken);

        String officerToken = staffTokenFor("officer@example.com", Role.LOAN_OFFICER);
        mockMvc.perform(put("/api/loans/loan-application")
                        .header("Authorization", "Bearer " + officerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new StatusChange(loanId, LoanStatus.APPROVED))))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/loans/loan-application")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Identifier(loanId))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Only pending loan applications can be deleted"));
    }

    @Test
    @DisplayName("updating an application that does not exist is 404")
    void unknownApplicationIsNotFound() throws Exception {
        String officerToken = staffTokenFor("officer@example.com", Role.LOAN_OFFICER);

        mockMvc.perform(put("/api/loans/loan-application")
                        .header("Authorization", "Bearer " + officerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new StatusChange(9999L, LoanStatus.APPROVED))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("the stored password is a BCrypt hash, never the plaintext")
    void passwordIsStoredHashed() throws Exception {
        register("ada@example.com", "longenough").andExpect(status().isOk());

        User stored = userRepository.findByEmail("ada@example.com").orElseThrow();
        assertThat(stored.getPasswordHash())
                .isNotEqualTo("longenough")
                .startsWith("$2");
        assertThat(passwordEncoder.matches("longenough", stored.getPasswordHash())).isTrue();
        assertThat(stored.getRole()).isEqualTo(Role.CUSTOMER);
    }

    private org.springframework.test.web.servlet.ResultActions register(String email, String password)
            throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Credentials(email, password))));
    }

    private String tokenFor(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Credentials(email, password))))
                .andExpect(status().isOk())
                .andReturn();

        return json(result).get("token").asText();
    }

    private String staffTokenFor(String email, Role role) throws Exception {
        User staff = new User();
        staff.setEmail(email);
        staff.setPasswordHash(passwordEncoder.encode("longenough"));
        staff.setRole(role);
        userRepository.save(staff);

        return tokenFor(email, "longenough");
    }

    private long createLoan(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/loans/loan-application")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 5000.00, "termMonths": 24}"""))
                .andExpect(status().isCreated())
                .andReturn();

        return json(result).get("id").asLong();
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private record Credentials(String email, String password) {
    }

    private record StatusChange(Long id, LoanStatus newStatus) {
    }

    private record Identifier(Long id) {
    }
}
