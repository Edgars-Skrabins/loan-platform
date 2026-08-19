package io.github.edgarsskrabins.loan_platform.loanApplication.service;

import io.github.edgarsskrabins.loan_platform.customer.entity.CustomerProfile;
import io.github.edgarsskrabins.loan_platform.customer.service.CustomerProfileService;
import io.github.edgarsskrabins.loan_platform.exceptions.CustomerProfileNotFoundException;
import io.github.edgarsskrabins.loan_platform.exceptions.ForbiddenOperationException;
import io.github.edgarsskrabins.loan_platform.exceptions.InvalidLoanStateException;
import io.github.edgarsskrabins.loan_platform.exceptions.LoanApplicationNotFoundException;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
    private CustomerProfileService customerProfileService;

    @InjectMocks
    private LoanApplicationService service;

    @Nested
    @DisplayName("createLoanApplication")
    class Create {

        @Test
        @DisplayName("attaches the caller's profile, amount and term, and starts PENDING")
        void createsApplicationForCurrentUser() {
            CustomerProfile profile = profile(PROFILE_ID);
            when(currentUserService.getCurrentUser()).thenReturn(user(Role.CUSTOMER));
            when(customerProfileService.getByUserId(USER_ID)).thenReturn(profile);
            when(loanApplicationRepository.save(any(LoanApplication.class)))
                    .thenAnswer(invocation -> {
                        LoanApplication application = invocation.getArgument(0);
                        application.setId(LOAN_ID);
                        return application;
                    });

            CreateLoanApplicationResponse response = service.createLoanApplication(
                    new CreateLoanApplicationRequest(new BigDecimal("5000.00"), 24));

            ArgumentCaptor<LoanApplication> saved = ArgumentCaptor.forClass(LoanApplication.class);
            verify(loanApplicationRepository).save(saved.capture());
            assertThat(saved.getValue().getCustomer()).isSameAs(profile);
            assertThat(saved.getValue().getAmount()).isEqualByComparingTo("5000.00");
            assertThat(saved.getValue().getTermMonths()).isEqualTo(24);
            assertThat(saved.getValue().getStatus()).isEqualTo(LoanStatus.PENDING);
            assertThat(response.id()).isEqualTo(LOAN_ID);
            assertThat(response.status()).isEqualTo(LoanStatus.PENDING);
        }

        @Test
        @DisplayName("throws and saves nothing when the caller has no customer profile")
        void failsWithoutCustomerProfile() {
            when(currentUserService.getCurrentUser()).thenReturn(user(Role.CUSTOMER));
            when(customerProfileService.getByUserId(USER_ID))
                    .thenThrow(new CustomerProfileNotFoundException(USER_ID));

            assertThatThrownBy(() -> service.createLoanApplication(
                    new CreateLoanApplicationRequest(new BigDecimal("5000.00"), 24)))
                    .isInstanceOf(CustomerProfileNotFoundException.class);
            verify(loanApplicationRepository, never()).save(any());
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
        @DisplayName("rejects a CUSTOMER caller and never loads the application")
        void customerCannotUpdateStatus() {
            when(currentUserService.getCurrentUser()).thenReturn(user(Role.CUSTOMER));

            assertThatThrownBy(() -> service.updateLoanApplicationStatus(
                    new UpdateLoanApplicationStatusRequest(LOAN_ID, LoanStatus.APPROVED)))
                    .isInstanceOf(ForbiddenOperationException.class)
                    .hasMessage("Only admins or loan officers can update loan application status");
            verify(loanApplicationRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws LoanApplicationNotFoundException for an unknown id")
        void failsOnUnknownApplication() {
            when(currentUserService.getCurrentUser()).thenReturn(user(Role.LOAN_OFFICER));
            when(loanApplicationRepository.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateLoanApplicationStatus(
                    new UpdateLoanApplicationStatusRequest(404L, LoanStatus.APPROVED)))
                    .isInstanceOf(LoanApplicationNotFoundException.class);
            verify(loanApplicationRepository, never()).save(any());
        }

        @Test
        @Disabled("Pending: status transitions are not validated as a state machine")
        @DisplayName("refuses to move a decided application back to PENDING")
        void cannotReopenDecidedApplication() {
            when(currentUserService.getCurrentUser()).thenReturn(user(Role.LOAN_OFFICER));
            when(loanApplicationRepository.findById(LOAN_ID))
                    .thenReturn(Optional.of(application(LoanStatus.APPROVED)));

            assertThatThrownBy(() -> service.updateLoanApplicationStatus(
                    new UpdateLoanApplicationStatusRequest(LOAN_ID, LoanStatus.PENDING)))
                    .isInstanceOf(InvalidLoanStateException.class);
            verify(loanApplicationRepository, never()).save(any());
        }

        @Test
        @Disabled("Pending: no LoanDecision or AuditLog row is written when a status changes")
        @DisplayName("records who made the decision")
        void recordsDecision() {
            throw new AssertionError("updateLoanApplicationStatus persists no LoanDecision");
        }
    }

    @Nested
    @DisplayName("deleteLoanApplication")
    class Delete {

        @Test
        @DisplayName("lets the owning customer delete their own PENDING application")
        void ownerCanDeletePendingApplication() {
            LoanApplication application = application(LoanStatus.PENDING);
            when(currentUserService.getCurrentUser()).thenReturn(user(Role.CUSTOMER));
            when(loanApplicationRepository.findById(LOAN_ID)).thenReturn(Optional.of(application));
            when(customerProfileService.getByUserId(USER_ID)).thenReturn(profile(PROFILE_ID));

            service.deleteLoanApplication(new DeleteLoanApplicationRequest(LOAN_ID));

            verify(loanApplicationRepository).delete(application);
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"LOAN_OFFICER", "ADMIN"})
        @DisplayName("lets staff delete a PENDING application without owning it")
        void staffCanDeletePendingApplication(Role role) {
            LoanApplication application = application(LoanStatus.PENDING);
            when(currentUserService.getCurrentUser()).thenReturn(user(role));
            when(loanApplicationRepository.findById(LOAN_ID)).thenReturn(Optional.of(application));

            service.deleteLoanApplication(new DeleteLoanApplicationRequest(LOAN_ID));

            verify(loanApplicationRepository).delete(application);
        }

        @ParameterizedTest
        @EnumSource(value = LoanStatus.class, names = "PENDING", mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("refuses to delete an application that has left PENDING")
        void refusesNonPendingApplication(LoanStatus status) {
            when(currentUserService.getCurrentUser()).thenReturn(user(Role.CUSTOMER));
            when(loanApplicationRepository.findById(LOAN_ID))
                    .thenReturn(Optional.of(application(status)));
            when(customerProfileService.getByUserId(USER_ID)).thenReturn(profile(PROFILE_ID));

            assertThatThrownBy(() -> service.deleteLoanApplication(new DeleteLoanApplicationRequest(LOAN_ID)))
                    .isInstanceOf(InvalidLoanStateException.class)
                    .hasMessage("Only pending loan applications can be deleted");
            verify(loanApplicationRepository, never()).delete(any());
        }

        @Test
        @DisplayName("throws LoanApplicationNotFoundException for an unknown id")
        void failsOnUnknownApplication() {
            when(currentUserService.getCurrentUser()).thenReturn(user(Role.CUSTOMER));
            when(loanApplicationRepository.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteLoanApplication(new DeleteLoanApplicationRequest(404L)))
                    .isInstanceOf(LoanApplicationNotFoundException.class);
            verify(loanApplicationRepository, never()).delete(any());
        }

        @Test
        @DisplayName("refuses to delete an application belonging to another customer")
        void refusesOtherCustomersApplication() {
            User attacker = user(Role.CUSTOMER);
            attacker.setId(999L);
            when(currentUserService.getCurrentUser()).thenReturn(attacker);
            when(loanApplicationRepository.findById(LOAN_ID))
                    .thenReturn(Optional.of(application(LoanStatus.PENDING)));
            when(customerProfileService.getByUserId(999L)).thenReturn(profile(222L));

            assertThatThrownBy(() -> service.deleteLoanApplication(new DeleteLoanApplicationRequest(LOAN_ID)))
                    .isInstanceOf(ForbiddenOperationException.class)
                    .hasMessage("You can only delete your own loan applications");
            verify(loanApplicationRepository, never()).delete(any());
        }
    }

    private static User user(Role role) {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail("ada@example.com");
        user.setRole(role);
        return user;
    }

    private static CustomerProfile profile(Long id) {
        CustomerProfile profile = new CustomerProfile();
        profile.setId(id);
        return profile;
    }

    private static LoanApplication application(LoanStatus status) {
        LoanApplication application = new LoanApplication();
        application.setId(LOAN_ID);
        application.setCustomer(profile(PROFILE_ID));
        application.setAmount(new BigDecimal("5000.00"));
        application.setTermMonths(24);
        application.setStatus(status);
        return application;
    }
}
