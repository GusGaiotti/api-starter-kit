package com.standard.backend.api.dto;

public record UserResponse(
        Long id,
        String email,
        String name,
        Boolean active
) {}

