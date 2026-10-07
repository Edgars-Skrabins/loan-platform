package io.github.edgarsskrabins.loan_platform.loanApplication.controller;

import io.github.edgarsskrabins.loan_platform.loanApplication.dto.createLoanApplication.CreateLoanApplicationRequest;
import io.github.edgarsskrabins.loan_platform.loanApplication.dto.createLoanApplication.CreateLoanApplicationResponse;
import io.github.edgarsskrabins.loan_platform.loanApplication.dto.getLoanApplication.GetLoanApplicationResponse;
import io.github.edgarsskrabins.loan_platform.loanApplication.dto.updateLoanApplication.UpdateLoanApplicationStatusRequest;
import io.github.edgarsskrabins.loan_platform.loanApplication.dto.updateLoanApplication.UpdateLoanApplicationStatusResponse;
import io.github.edgarsskrabins.loan_platform.loanApplication.service.LoanApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/loans")
@RequiredArgsConstructor
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    @PostMapping("/loan-application")
    public CreateLoanApplicationResponse createLoanApplication(
            @RequestBody @Valid CreateLoanApplicationRequest request
    ) {
        return loanApplicationService.createLoanApplication(request);
    }

    @GetMapping("/loan-applications")
    public List<GetLoanApplicationResponse> getLoanApplications() {
        return loanApplicationService.getLoanApplications();
    }

    @GetMapping("/loan-application/{id}")
    public GetLoanApplicationResponse getLoanApplication(@PathVariable Long id) {
        return loanApplicationService.getLoanApplication(id);
    }

    @PutMapping("/loan-application/{id}")
    public UpdateLoanApplicationStatusResponse updateLoanApplicationStatus(
            @PathVariable Long id,
            @RequestBody @Valid UpdateLoanApplicationStatusRequest request
    ) {
        return loanApplicationService.updateLoanApplicationStatus(id, request);
    }

    @DeleteMapping("/loan-application/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLoanApplication(@PathVariable Long id) {
        loanApplicationService.deleteLoanApplication(id);
    }

}
