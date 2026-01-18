package com.example.splitwise_cc_backend.controller;

import com.example.splitwise_cc_backend.model.User;
import com.example.splitwise_cc_backend.model.response.UserLoginDTO;
import com.example.splitwise_cc_backend.model.request.LoginRequest;
import com.example.splitwise_cc_backend.model.request.RegisterRequest;
import com.example.splitwise_cc_backend.model.response.TokenResponse;
import com.example.splitwise_cc_backend.repository.UserRepository;
import com.example.splitwise_cc_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public TokenResponse register(@RequestBody RegisterRequest req) {
        return new TokenResponse(
                authService.register(req.getName(), req.getEmail(), req.getPassword())
        );
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest req) {
        return new TokenResponse(
                authService.login(req.getEmail(), req.getPassword())
        );
    }

    @GetMapping("/me")
    public UserLoginDTO me(Authentication authentication) {
        UserLoginDTO principal = (UserLoginDTO) authentication.getPrincipal();
        return principal;
    }
}
