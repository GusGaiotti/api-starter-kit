package com.standard.backend.api.dto;

public record AuthResponse(
        String token,
        String email,
        String name
) {}
