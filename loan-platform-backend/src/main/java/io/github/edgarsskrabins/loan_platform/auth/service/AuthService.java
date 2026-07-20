package io.github.edgarsskrabins.loan_platform.auth.service;

import io.github.edgarsskrabins.loan_platform.auth.dto.RegisterRequest;
import io.github.edgarsskrabins.loan_platform.auth.dto.RegisterResponse;
import io.github.edgarsskrabins.loan_platform.user.entity.User;
import io.github.edgarsskrabins.loan_platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterResponse register(RegisterRequest request) {
        User user = new User();

        user.setEmail(request.email());
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getCreatedAt()
        );
    }
}
