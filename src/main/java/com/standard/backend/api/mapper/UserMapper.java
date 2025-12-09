package com.standard.backend.api.mapper;

import com.standard.backend.api.dto.CreateUserRequest;
import com.standard.backend.api.dto.UserResponse;
import com.standard.backend.api.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(CreateUserRequest request) {
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setName(request.name());
        return user;
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getActive()
        );
    }
}
