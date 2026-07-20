package io.github.edgarsskrabins.loan_platform.loanApplication.repository;

import io.github.edgarsskrabins.loan_platform.loanApplication.entity.LoanDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanDecisionRepository extends JpaRepository<LoanDecision, Long> {

    List<LoanDecision> findByLoanApplicationId(Long loanId);

}
