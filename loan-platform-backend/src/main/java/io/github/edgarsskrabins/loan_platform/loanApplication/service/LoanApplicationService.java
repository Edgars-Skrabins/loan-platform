package io.github.edgarsskrabins.loan_platform.loanApplication.service;

import io.github.edgarsskrabins.loan_platform.customer.entity.CustomerProfile;
import io.github.edgarsskrabins.loan_platform.customer.service.CustomerProfileService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoanApplicationService {

    private final CurrentUserService currentUserService;
    private final LoanApplicationRepository loanApplicationRepository;
    private final CustomerProfileService customerProfileService;

    @Transactional
    public CreateLoanApplicationResponse createLoanApplication(CreateLoanApplicationRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        CustomerProfile customerProfile = customerProfileService.getByUserId(currentUser.getId());

        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setCustomer(customerProfile);
        loanApplication.setAmount(request.amount());
        loanApplication.setTermMonths(request.termMonths());

        LoanApplication savedLoanApplication = loanApplicationRepository.save(loanApplication);

        return new CreateLoanApplicationResponse(
                savedLoanApplication.getId(),
                savedLoanApplication.getStatus()
        );
    }

    @Transactional
    public UpdateLoanApplicationStatusResponse updateLoanApplicationStatus(UpdateLoanApplicationStatusRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        if (currentUser.getRole() == Role.CUSTOMER) {
            throw new ForbiddenOperationException(
                    "Only admins or loan officers can update loan application status");
        }

        LoanApplication loanApplication = findOrThrow(request.id());
        loanApplication.setStatus(request.newStatus());

        LoanApplication savedLoanApplication = loanApplicationRepository.save(loanApplication);

        return new UpdateLoanApplicationStatusResponse(
                savedLoanApplication.getId(),
                savedLoanApplication.getStatus());
    }

    @Transactional
    public void deleteLoanApplication(DeleteLoanApplicationRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        LoanApplication loanApplication = findOrThrow(request.id());

        if (currentUser.getRole() == Role.CUSTOMER) {
            Long callerProfileId = customerProfileService.getByUserId(currentUser.getId()).getId();
            if (!callerProfileId.equals(loanApplication.getCustomer().getId())) {
                throw new ForbiddenOperationException(
                        "You can only delete your own loan applications");
            }
        }

        if (loanApplication.getStatus() != LoanStatus.PENDING) {
            throw new InvalidLoanStateException("Only pending loan applications can be deleted");
        }

        loanApplicationRepository.delete(loanApplication);
    }

    private LoanApplication findOrThrow(Long id) {
        return loanApplicationRepository.findById(id)
                .orElseThrow(() -> new LoanApplicationNotFoundException(id));
    }
}
