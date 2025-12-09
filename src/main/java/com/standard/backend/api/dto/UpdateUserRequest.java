package com.standard.backend.api.dto;

public record UpdateUserRequest(
        String name,
        Boolean active
) {}
