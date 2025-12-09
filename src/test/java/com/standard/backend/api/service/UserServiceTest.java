package com.standard.backend.api.service;

import com.standard.backend.api.dto.CreateUserRequest;
import com.standard.backend.api.dto.UserResponse;
import com.standard.backend.api.entity.User;
import com.standard.backend.api.mapper.UserMapper;
import com.standard.backend.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository repository;
    @Mock private UserMapper mapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserService service;

    @Test
    void shouldSoftDeleteUser_WhenDeleteIsCalled() {
        Long userId = 1L;
        User activeUser = new User();
        activeUser.setId(userId);
        activeUser.setActive(true);

        when(repository.findById(userId)).thenReturn(Optional.of(activeUser));

        service.delete(userId);

        assertFalse(activeUser.getActive(), "User should be inactive (active=false)");

        verify(repository, times(1)).save(activeUser);
        verify(repository, never()).deleteById(any());
    }

    @Test
    void shouldCreateUser_WithEncryptedPassword() {
        CreateUserRequest request = new CreateUserRequest("test@email.com", "123456", "Test User");
        User userEntity = new User();
        userEntity.setEmail("test@email.com");

        when(repository.existsByEmail(any())).thenReturn(false);
        when(mapper.toEntity(any())).thenReturn(userEntity);
        when(passwordEncoder.encode("123456")).thenReturn("ENCRYPTED_HASH");
        when(repository.save(any())).thenReturn(userEntity);
        when(mapper.toResponse(any())).thenReturn(new UserResponse(1L, "test@email.com", "Test User", true));

        service.create(request);

        verify(passwordEncoder).encode("123456");
        verify(repository).save(userEntity);
    }
}