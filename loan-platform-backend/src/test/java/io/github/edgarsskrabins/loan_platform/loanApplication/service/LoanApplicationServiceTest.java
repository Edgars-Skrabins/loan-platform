package io.github.edgarsskrabins.loan_platform.loanApplication.service;

import io.github.edgarsskrabins.loan_platform.customer.entity.CustomerProfile;
import io.github.edgarsskrabins.loan_platform.customer.repository.CustomerProfileRepository;
import io.github.edgarsskrabins.loan_platform.loanApplication.dto.createLoanApplication.CreateLoanApplicationRequest;
import io.github.edgarsskrabins.loan_platform.loanApplication.dto.createLoanApplication.CreateLoanApplicationResponse;
import io.github.edgarsskrabins.loan_platform.loanApplication.dto.deleteLoanApplication.DeleteLoanApplicationRequest;
import io.github.edgarsskrabins.loan_platform.loanApplication.dto.updateLoanApplication.UpdateLoanApplicationStatusRequest;
import io.github.edgarsskrabins.loan_platform.loanApplication.dto.updateLoanApplication.UpdateLoanApplicationStatusResponse;
import io.github.edgarsskrabins.loan_platform.loanApplication.entity.LoanApplication;
import io.github.edgarsskrabins.loan_platform.loanApplication.entity.LoanStatus;
import io.github.edgarsskrabins.loan_platform.loanApplication.repository.LoanApplicationRepository;
import io.github.edgarsskrabins.loan_platform.security.CurrentUserService;
import io.github.edgarsskrabins.loan_platform.user.entity.Role;
import io.github.edgarsskrabins.loan_platform.user.entity.User;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanApplicationServiceTest {

    private static final long USER_ID = 7L;
    private static final long PROFILE_ID = 11L;
    private static final long LOAN_ID = 100L;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private CustomerProfileRepository customerProfileRepository;

    @InjectMocks
    private LoanApplicationService service;

    @Nested
    @DisplayName("createLoanApplication")
    class Create {

        @Test
        @DisplayName("attaches the caller's profile and the requested amount, and starts PENDING")
        void createsApplicationForCurrentUser() {
            CustomerProfile profile = profile();
            when(currentUserService.getCurrentUser()).thenReturn(user(Role.CUSTOMER));
            when(customerProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(profile));
            when(loanApplicationRepository.save(any(LoanApplication.class)))
                    .thenAnswer(invocation -> {
                        LoanApplication application = invocation.getArgument(0);
                        application.setId(LOAN_ID);
                        return application;
                    });

            CreateLoanApplicationResponse response =
                    service.createLoanApplication(new CreateLoanApplicationRequest(new BigDecimal("5000.00")));

            ArgumentCaptor<LoanApplication> saved = ArgumentCaptor.forClass(LoanApplication.class);
            verify(loanApplicationRepository).save(saved.capture());
            assertThat(saved.getValue().getCustomer()).isSameAs(profile);
            assertThat(saved.getValue().getAmount()).isEqualByComparingTo("5000.00");
            assertThat(saved.getValue().getStatus()).isEqualTo(LoanStatus.PENDING);
            assertThat(response.id()).isEqualTo(LOAN_ID);
            assertThat(response.status()).isEqualTo(LoanStatus.PENDING);
        }

        @Test
        @DisplayName("throws and saves nothing when the caller has no customer profile")
        void failsWithoutCustomerProfile() {
            when(currentUserService.getCurrentUser()).thenReturn(user(Role.CUSTOMER));
            when(customerProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createLoanApplication(
                    new CreateLoanApplicationRequest(new BigDecimal("5000.00"))))
                    .isInstanceOf(NoSuchElementException.class);
            verify(loanApplicationRepository, never()).save(any());
        }

        @Test
        @Disabled("""
                BUG: createLoanApplication never sets termMonths, but loan_applications.term_months
                is NOT NULL in V1__init.sql, so every create fails at flush time. termMonths (and
                interestRate) are also absent from CreateLoanApplicationRequest. Remove @Disabled
                once the request carries a term and the service copies it across.""")
        @DisplayName("copies the requested term onto the application")
        void copiesTermMonths() {
            when(currentUserService.getCurrentUser()).thenReturn(user(Role.CUSTOMER));
            when(customerProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(profile()));
            when(loanApplicationRepository.save(any(LoanApplication.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            service.createLoanApplication(new CreateLoanApplicationRequest(new BigDecimal("5000.00")));

            ArgumentCaptor<LoanApplication> saved = ArgumentCaptor.forClass(LoanApplication.class);
            verify(loanApplicationRepository).save(saved.capture());
            assertThat(saved.getValue().getTermMonths()).isNotNull();
        }
    }

    @Nested
    @DisplayName("updateLoanApplicationStatus")
    class UpdateStatus {

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"LOAN_OFFICER", "ADMIN"})
        @DisplayName("lets staff move an application to a new status")
        void staffCanUpdateStatus(Role role) {
            LoanApplication application = application(LoanStatus.PENDING);
            when(currentUserService.getCurrentUser()).thenReturn(user(role));
            when(loanApplicationRepository.findById(LOAN_ID)).thenReturn(Optional.of(application));
            when(loanApplicationRepository.save(application)).thenReturn(application);

            UpdateLoanApplicationStatusResponse response = service.updateLoanApplicationStatus(
                    new UpdateLoanApplicationStatusRequest(LOAN_ID, LoanStatus.APPROVED));

            assertThat(application.getStatus()).isEqualTo(LoanStatus.APPROVED);
            assertThat(response.id()).isEqualTo(LOAN_ID);
            assertThat(response.newStatus()).isEqualTo(LoanStatus.APPROVED);
        }

        @Test
        @DisplayName("rejects a CUSTOMER caller and leaves the application untouched")
        void customerCannotUpdateStatus() {
            when(currentUserService.getCurrentUser()).thenReturn(user(Role.CUSTOMER));

            assertThatThrownBy(() -> service.updateLoanApplicationStatus(
                    new UpdateLoanApplicationStatusRequest(LOAN_ID, LoanStatus.APPROVED)))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Only admins or loan officers can update loan application status");
            verify(loanApplicationRepository, never()).findById(anyLong());
            verify(loanApplicationRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when the application does not exist")
        void failsOnUnknownApplication() {
            when(currentUserService.getCurrentUser()).thenReturn(user(Role.LOAN_OFFICER));
            when(loanApplicationRepository.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateLoanApplicationStatus(
                    new UpdateLoanApplicationStatusRequest(404L, LoanStatus.APPROVED)))
                    .isInstanceOf(NoSuchElementException.class);
            verify(loanApplicationRepository, never()).save(any());
        }

        @Test
        @Disabled("""
                BUG: any status can move to any other status, so a decided loan can be walked back
                to PENDING (or re-decided repeatedly). V1__init.sql documents loan_decisions as an
                append-only decision history for exactly this reason. Remove @Disabled once
                transitions are validated as a state machine.""")
        @DisplayName("refuses to move a decided application back to PENDING")
        void cannotReopenDecidedApplication() {
            when(currentUserService.getCurrentUser()).thenReturn(user(Role.LOAN_OFFICER));
            when(loanApplicationRepository.findById(LOAN_ID))
                    .thenReturn(Optional.of(application(LoanStatus.APPROVED)));

            assertThatThrownBy(() -> service.updateLoanApplicationStatus(
                    new UpdateLoanApplicationStatusRequest(LOAN_ID, LoanStatus.PENDING)))
                    .isInstanceOf(IllegalStateException.class);
            verify(loanApplicationRepository, never()).save(any());
        }

        @Test
        @Disabled("""
                BUG: approving/rejecting writes nothing to loan_decisions and nothing to audit_logs,
                so LoanDecisionRepository, AuditLogRepository and AuditAction are all dead code and
                there is no record of who decided what. Remove @Disabled once a decision row is
                persisted alongside the status change.""")
        @DisplayName("records who made the decision")
        void recordsDecision() {
            // Cannot be asserted yet: LoanApplicationService has no LoanDecisionRepository
            // dependency to verify against. Wire one in, then assert the saved decision's
            // officer, outcome and comment here.
            throw new AssertionError("updateLoanApplicationStatus persists no LoanDecision");
        }

        @Test
        @Disabled("""
                BUG: UpdateLoanApplicationStatusRequest.newStatus has no @NotNull, and the service
                does not check it, so a null status is written straight to a NOT NULL column.
                Remove @Disabled once a null target status is rejected.""")
        @DisplayName("rejects a null target status")
        void rejectsNullStatus() {
            when(currentUserService.getCurrentUser()).thenReturn(user(Role.LOAN_OFFICER));
            when(loanApplicationRepository.findById(LOAN_ID))
                    .thenReturn(Optional.of(application(LoanStatus.PENDING)));

            assertThatThrownBy(() -> service.updateLoanApplicationStatus(
                    new UpdateLoanApplicationStatusRequest(LOAN_ID, null)))
                    .isInstanceOf(IllegalArgumentException.class);
            verify(loanApplicationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteLoanApplication")
    class Delete {

        @Test
        @DisplayName("deletes an application that is still PENDING")
        void deletesPendingApplication() {
            when(loanApplicationRepository.findById(LOAN_ID))
                    .thenReturn(Optional.of(application(LoanStatus.PENDING)));

            service.deleteLoanApplication(new DeleteLoanApplicationRequest(LOAN_ID));

            verify(loanApplicationRepository).deleteById(LOAN_ID);
        }

        @ParameterizedTest
        @EnumSource(value = LoanStatus.class, names = "PENDING", mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("refuses to delete an application that has left PENDING")
        void refusesNonPendingApplication(LoanStatus status) {
            when(loanApplicationRepository.findById(LOAN_ID))
                    .thenReturn(Optional.of(application(status)));

            assertThatThrownBy(() -> service.deleteLoanApplication(new DeleteLoanApplicationRequest(LOAN_ID)))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Only pending loan applications can be deleted");
            verify(loanApplicationRepository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("throws when the application does not exist")
        void failsOnUnknownApplication() {
            when(loanApplicationRepository.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteLoanApplication(new DeleteLoanApplicationRequest(404L)))
                    .isInstanceOf(NoSuchElementException.class);
            verify(loanApplicationRepository, never()).deleteById(anyLong());
        }

        @Test
        @Disabled("""
                SECURITY BUG: deleteLoanApplication never consults CurrentUserService, so any
                authenticated user can delete any other customer's PENDING application just by
                guessing its id (IDOR). Remove @Disabled once ownership is enforced.""")
        @DisplayName("refuses to delete an application belonging to another customer")
        void refusesOtherCustomersApplication() {
            User attacker = user(Role.CUSTOMER);
            attacker.setId(999L);
            when(currentUserService.getCurrentUser()).thenReturn(attacker);
            when(loanApplicationRepository.findById(LOAN_ID))
                    .thenReturn(Optional.of(application(LoanStatus.PENDING)));

            assertThatThrownBy(() -> service.deleteLoanApplication(new DeleteLoanApplicationRequest(LOAN_ID)))
                    .isInstanceOf(RuntimeException.class);
            verify(loanApplicationRepository, never()).deleteById(anyLong());
        }
    }

    private static User user(Role role) {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail("ada@example.com");
        user.setRole(role);
        return user;
    }

    private static CustomerProfile profile() {
        CustomerProfile profile = new CustomerProfile();
        profile.setId(PROFILE_ID);
        profile.setUser(user(Role.CUSTOMER));
        return profile;
    }

    private static LoanApplication application(LoanStatus status) {
        LoanApplication application = new LoanApplication();
        application.setId(LOAN_ID);
        application.setCustomer(profile());
        application.setAmount(new BigDecimal("5000.00"));
        application.setTermMonths(24);
        application.setStatus(status);
        return application;
    }
}
