package com.example.splitwise_cc_backend.service;

import com.example.splitwise_cc_backend.exception.ConflictException;
import com.example.splitwise_cc_backend.model.User;
import com.example.splitwise_cc_backend.repository.UserRepository;
import com.example.splitwise_cc_backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public String register(String name, String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ConflictException("Email already registered");
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .build();

        userRepository.save(user);
        jwtUtil.generateToken(user.getId(), user.getEmail(), user.getName());
        return "User registered successfully.";
    }

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ConflictException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return jwtUtil.generateToken(user.getId(), user.getEmail(), user.getName());
    }
}
