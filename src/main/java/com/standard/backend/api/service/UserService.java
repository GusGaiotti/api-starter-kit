package com.standard.backend.api.service;

import com.standard.backend.api.dto.CreateUserRequest;
import com.standard.backend.api.dto.UpdateUserRequest;
import com.standard.backend.api.dto.UserResponse;
import com.standard.backend.api.entity.User;
import com.standard.backend.api.exception.BusinessException;
import com.standard.backend.api.exception.ResourceNotFoundException;
import com.standard.backend.api.mapper.UserMapper;
import com.standard.backend.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository repository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        log.info("Attempting to create user with email: {}", request.email());

        if (repository.existsByEmail(request.email())) {
            log.warn("Creation failed. Email already exists: {}", request.email());
            throw new BusinessException("Email already exists");
        }

        User user = mapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setActive(true);

        User saved = repository.save(user);

        log.info("User created successfully with ID: {}", saved.getId());
        return mapper.toResponse(saved);
    }

    public UserResponse findById(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapper.toResponse(user);
    }

    public Page<UserResponse> findAll(Pageable pageable) {
        log.debug("Fetching active users with pagination: {}", pageable);

        return repository.findByActiveTrue(pageable)
                .map(mapper::toResponse);
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!user.getEmail().equals(currentUserEmail)) {
            throw new BusinessException("You are not authorized to update this user.");
        }

        if (request.name() != null) {
            user.setName(request.name());
        }
        if (request.active() != null) {
            user.setActive(request.active());
            log.info("User ID {} status changed to: {}", id, request.active());
        }

        User updated = repository.save(user);
        return mapper.toResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Request to deactivate (Soft Delete) user ID: {}", id);

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!user.getEmail().equals(currentUserEmail)) {
            throw new BusinessException("You are not authorized to delete this user.");
        }

        user.setActive(false);
        repository.save(user);

        log.info("User ID {} deactivated successfully.", id);
    }


}