package com.standard.backend.api.service;

import com.standard.backend.api.dto.AuthResponse;
import com.standard.backend.api.dto.LoginRequest;
import com.standard.backend.api.entity.User;
import com.standard.backend.api.exception.BusinessException;
import com.standard.backend.api.repository.UserRepository;
import com.standard.backend.api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("Invalid credentials"));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new BusinessException("Account is disabled");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getName());
    }
}
