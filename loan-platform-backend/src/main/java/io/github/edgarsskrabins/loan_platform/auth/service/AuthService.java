package io.github.edgarsskrabins.loan_platform.auth.service;

import io.github.edgarsskrabins.loan_platform.auth.dto.login.LoginRequest;
import io.github.edgarsskrabins.loan_platform.auth.dto.login.LoginResponse;
import io.github.edgarsskrabins.loan_platform.auth.dto.register.RegisterRequest;
import io.github.edgarsskrabins.loan_platform.auth.dto.register.RegisterResponse;
import io.github.edgarsskrabins.loan_platform.customer.service.CustomerProfileService;
import io.github.edgarsskrabins.loan_platform.exceptions.EmailAlreadyInUseException;
import io.github.edgarsskrabins.loan_platform.security.jwt.JwtService;
import io.github.edgarsskrabins.loan_platform.user.entity.Role;
import io.github.edgarsskrabins.loan_platform.user.entity.User;
import io.github.edgarsskrabins.loan_platform.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final CustomerProfileService customerProfileService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userService.existsByEmail(request.email())) {
            throw new EmailAlreadyInUseException(request.email());
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.CUSTOMER);

        User savedUser = userService.save(user);
        customerProfileService.createFor(savedUser);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userService.getUserByEmail(request.email());

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                user.getId(),
                token,
                user.getEmail(),
                user.getRole()
        );
    }
}
