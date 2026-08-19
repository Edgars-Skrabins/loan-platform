package io.github.edgarsskrabins.loan_platform.customer.service;

import io.github.edgarsskrabins.loan_platform.customer.entity.CustomerProfile;
import io.github.edgarsskrabins.loan_platform.customer.repository.CustomerProfileRepository;
import io.github.edgarsskrabins.loan_platform.exceptions.CustomerProfileNotFoundException;
import io.github.edgarsskrabins.loan_platform.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerProfileService {

    private final CustomerProfileRepository customerProfileRepository;

    @Transactional
    public CustomerProfile createFor(User user) {
        CustomerProfile profile = new CustomerProfile();
        profile.setUser(user);
        return customerProfileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public CustomerProfile getByUserId(Long userId) {
        return customerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomerProfileNotFoundException(userId));
    }
}
