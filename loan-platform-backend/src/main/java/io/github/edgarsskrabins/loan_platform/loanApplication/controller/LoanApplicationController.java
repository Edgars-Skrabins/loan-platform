package io.github.edgarsskrabins.loan_platform.loanApplication.controller;

import io.github.edgarsskrabins.loan_platform.loanApplication.dto.createLoanApplication.CreateLoanApplicationRequest;
import io.github.edgarsskrabins.loan_platform.loanApplication.dto.createLoanApplication.CreateLoanApplicationResponse;
import io.github.edgarsskrabins.loan_platform.loanApplication.dto.deleteLoanApplication.DeleteLoanApplicationRequest;
import io.github.edgarsskrabins.loan_platform.loanApplication.dto.updateLoanApplication.UpdateLoanApplicationStatusRequest;
import io.github.edgarsskrabins.loan_platform.loanApplication.dto.updateLoanApplication.UpdateLoanApplicationStatusResponse;
import io.github.edgarsskrabins.loan_platform.loanApplication.service.LoanApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/loan-application/{id}")
    public UpdateLoanApplicationStatusResponse updateLoanApplicationStatus(
            @RequestBody @Valid UpdateLoanApplicationStatusRequest request
    ) {
        return loanApplicationService.updateLoanApplicationStatus(request);
    }

    @DeleteMapping("/loan-application/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLoanApplication(
            @RequestBody @Valid DeleteLoanApplicationRequest request
    ) {
        loanApplicationService.deleteLoanApplication(request);
    }

}
