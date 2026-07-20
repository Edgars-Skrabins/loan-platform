package io.github.edgarsskrabins.loan_platform.loanApplication.service;

import io.github.edgarsskrabins.loan_platform.customer.entity.CustomerProfile;
import io.github.edgarsskrabins.loan_platform.customer.repository.CustomerProfileRepository;
import io.github.edgarsskrabins.loan_platform.loanApplication.dto.CreateLoanApplicationRequest;
import io.github.edgarsskrabins.loan_platform.loanApplication.dto.CreateLoanApplicationResponse;
import io.github.edgarsskrabins.loan_platform.loanApplication.entity.LoanApplication;
import io.github.edgarsskrabins.loan_platform.loanApplication.repository.LoanApplicationRepository;
import io.github.edgarsskrabins.loan_platform.security.CurrentUserService;
import io.github.edgarsskrabins.loan_platform.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoanApplicationService {

    private final CurrentUserService currentUserService;
    private final LoanApplicationRepository loanApplicationRepository;
    private final CustomerProfileRepository customerProfileRepository;

    public CreateLoanApplicationResponse createLoanApplication(CreateLoanApplicationRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        LoanApplication loanApplication = new LoanApplication();
        CustomerProfile customerProfile = customerProfileRepository.findByUserId(currentUser.getId()).orElseThrow();

        loanApplication.setCustomer(customerProfile);
        loanApplication.setAmount(request.amount());

        LoanApplication savedLoanApplication = loanApplicationRepository.save(loanApplication);

        return new CreateLoanApplicationResponse(
                savedLoanApplication.getId(),
                savedLoanApplication.getStatus()
        );
    }
}
