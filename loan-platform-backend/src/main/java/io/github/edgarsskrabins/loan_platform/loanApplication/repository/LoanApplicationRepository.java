package io.github.edgarsskrabins.loan_platform.loanApplication.repository;

import io.github.edgarsskrabins.loan_platform.loanApplication.entity.LoanApplication;
import io.github.edgarsskrabins.loan_platform.loanApplication.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    List<LoanApplication> findByCustomerId(Long customerId);

    List<LoanApplication> findByStatus(LoanStatus status);
}
