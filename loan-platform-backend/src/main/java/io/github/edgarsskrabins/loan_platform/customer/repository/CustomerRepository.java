package io.github.edgarsskrabins.loan_platform.customer.repository;

import io.github.edgarsskrabins.loan_platform.customer.entity.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<CustomerProfile, Long> {

    Optional<CustomerProfile> findByUserId(Long userId);

}
