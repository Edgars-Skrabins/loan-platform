package io.github.edgarsskrabins.loan_platform.auth.service;

import io.github.edgarsskrabins.loan_platform.auth.dto.login.LoginRequest;
import io.github.edgarsskrabins.loan_platform.auth.dto.login.LoginResponse;
import io.github.edgarsskrabins.loan_platform.auth.dto.register.RegisterRequest;
import io.github.edgarsskrabins.loan_platform.auth.dto.register.RegisterResponse;
import io.github.edgarsskrabins.loan_platform.security.jwt.JwtService;
import io.github.edgarsskrabins.loan_platform.user.entity.User;
import io.github.edgarsskrabins.loan_platform.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public RegisterResponse register(RegisterRequest request) {
        User user = new User();

        user.setEmail(request.email());
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );

        User savedUser = userService.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getCreatedAt()
        );
    }

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
