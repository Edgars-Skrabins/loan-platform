package io.github.edgarsskrabins.loan_platform.auth.controller;

import io.github.edgarsskrabins.loan_platform.auth.dto.RegisterRequest;
import io.github.edgarsskrabins.loan_platform.auth.dto.RegisterResponse;
import io.github.edgarsskrabins.loan_platform.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public RegisterResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return authService.register(request);
    }
}
