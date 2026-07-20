package io.github.edgarsskrabins.loan_platform.loanApplication.controller;

import io.github.edgarsskrabins.loan_platform.loanApplication.dto.CreateLoanApplicationRequest;
import io.github.edgarsskrabins.loan_platform.loanApplication.dto.CreateLoanApplicationResponse;
import io.github.edgarsskrabins.loan_platform.loanApplication.service.LoanApplicationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/loans")
public class LoanApplicationController {

    LoanApplicationService loanApplicationService;

    @PostMapping("/loan-application")
    public CreateLoanApplicationResponse createLoanApplication(
            @RequestBody @Valid CreateLoanApplicationRequest request
    ) {
        return loanApplicationService.createLoanApplication(request);
    }
}
