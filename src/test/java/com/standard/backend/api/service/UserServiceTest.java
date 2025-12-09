package com.standard.backend.api.service;

import com.standard.backend.api.dto.CreateUserRequest;
import com.standard.backend.api.dto.UpdateUserRequest;
import com.standard.backend.api.dto.UserResponse;
import com.standard.backend.api.entity.User;
import com.standard.backend.api.exception.BusinessException;
import com.standard.backend.api.exception.ResourceNotFoundException;
import com.standard.backend.api.mapper.UserMapper;
import com.standard.backend.api.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext(String email) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Should create user successfully with encrypted password")
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

    @Test
    @DisplayName("Should throw exception when creating user with existing email")
    void shouldThrowException_WhenEmailAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest("duplicate@email.com", "123456", "User");

        when(repository.existsByEmail(request.email())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(request));

        assertEquals("Email already exists", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should soft delete user when the requester is the owner")
    void shouldSoftDeleteUser_WhenUserIsOwner() {
        Long userId = 1L;
        String userEmail = "owner@email.com";

        User activeUser = new User();
        activeUser.setId(userId);
        activeUser.setEmail(userEmail);
        activeUser.setActive(true);

        mockSecurityContext(userEmail);

        when(repository.findById(userId)).thenReturn(Optional.of(activeUser));

        service.delete(userId);

        assertFalse(activeUser.getActive(), "User should be inactive (active=false)");
        verify(repository).save(activeUser);
    }

    @Test
    @DisplayName("Should throw exception when attempting to delete another user's account")
    void shouldThrowException_WhenDeletingOtherUserAccount() {
        Long userId = 1L;
        String victimEmail = "victim@email.com";
        String hackerEmail = "hacker@email.com";

        User victimUser = new User();
        victimUser.setId(userId);
        victimUser.setEmail(victimEmail);
        victimUser.setActive(true);

        mockSecurityContext(hackerEmail);

        when(repository.findById(userId)).thenReturn(Optional.of(victimUser));

        BusinessException exception = assertThrows(BusinessException.class, () -> service.delete(userId));

        assertEquals("You are not authorized to delete this user.", exception.getMessage());

        assertTrue(victimUser.getActive());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should update user when the requester is the owner")
    void shouldUpdateUser_WhenUserIsOwner() {
        Long userId = 1L;
        String email = "me@email.com";
        UpdateUserRequest request = new UpdateUserRequest("New Name", true);

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmail(email);
        existingUser.setName("Old Name");

        mockSecurityContext(email);

        when(repository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(repository.save(any())).thenReturn(existingUser);
        when(mapper.toResponse(any())).thenReturn(new UserResponse(1L, email, "New Name", true));

        service.update(userId, request);

        assertEquals("New Name", existingUser.getName());
        verify(repository).save(existingUser);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user does not exist")
    void shouldThrowNotFound_WhenUserDoesNotExist() {
        Long userId = 99L;

        when(repository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(userId));
    }
}