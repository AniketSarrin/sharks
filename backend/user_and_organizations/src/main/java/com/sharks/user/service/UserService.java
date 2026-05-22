package com.sharks.user.service;

import com.sharks.user.dto.UserDto;
import com.sharks.user.dto.UserUpdateRequest;
import com.sharks.user.entity.UserProfile;
import com.sharks.user.entity.UserRole;
import com.sharks.user.exception.UserNotFoundException;
import com.sharks.user.messaging.UserEventPublisher;
import com.sharks.user.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserProfileRepository userProfileRepository;
    private final UserEventPublisher userEventPublisher;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserProfileRepository userProfileRepository,
            UserEventPublisher userEventPublisher,
            PasswordEncoder passwordEncoder) {
        this.userProfileRepository = userProfileRepository;
        this.userEventPublisher = userEventPublisher;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDto getCurrentUser(String userId) {
        return toDto(findProfile(userId));
    }

    public UserDto updateCurrentUser(String userId, UserUpdateRequest request) {
        String normalizedUserId = requireUserId(userId);
        validatePutRequest(request);

        UUID id = UUID.fromString(normalizedUserId);
        Optional<UserProfile> existing = userProfileRepository.findById(id);
        boolean isNew = existing.isEmpty();
        UserProfile profile = existing.orElseGet(() -> {
            UserProfile newProfile = new UserProfile();
            newProfile.setId(id);
            return newProfile;
        });

        String plainPasswordForAuth = request.getPassword();
        applyRequest(profile, request, true);
        userProfileRepository.save(profile);

        if (isNew && plainPasswordForAuth != null && !plainPasswordForAuth.isBlank()) {
            userEventPublisher.publishUserCreated(
                    id,
                    profile.getEmail(),
                    plainPasswordForAuth,
                    profile.getRole().name());
        } else if (isNew) {
            log.warn("Skipping user.created publish for id={}: password required for auth provisioning", id);
        }

        return toDto(profile);
    }

    public UserDto patchCurrentUser(String userId, UserUpdateRequest request) {
        UserProfile profile = findProfile(userId);
        applyRequest(profile, request, false);
        userProfileRepository.save(profile);
        return toDto(profile);
    }

    public void deleteCurrentUser(String userId) {
        UserProfile profile = findProfile(userId);
        userEventPublisher.publishUserDeletedForAuth(profile.getEmail());
        userProfileRepository.delete(profile);
    }

    private UserProfile findProfile(String userId) {
        UUID id = UUID.fromString(requireUserId(userId));
        return userProfileRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
    }

    private String requireUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("X-User-Id header is required");
        }
        return userId;
    }

    private void validatePutRequest(UserUpdateRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new IllegalArgumentException("Full name must not be blank");
        }
    }

    private void applyRequest(UserProfile profile, UserUpdateRequest request, boolean replace) {
        if (replace) {
            profile.setEmail(request.getEmail());
            profile.setFullName(request.getFullName());
        } else {
            if (request.getEmail() != null) {
                if (request.getEmail().isBlank()) {
                    throw new IllegalArgumentException("Email must not be blank");
                }
                profile.setEmail(request.getEmail());
            }
            if (request.getFullName() != null) {
                if (request.getFullName().isBlank()) {
                    throw new IllegalArgumentException("Full name must not be blank");
                }
                profile.setFullName(request.getFullName());
            }
        }

        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getPhone() != null) {
            profile.setPhone(request.getPhone());
        }
        if (request.getLocation() != null) {
            profile.setLocation(request.getLocation());
        }
        if (request.getRole() != null) {
            profile.setRole(UserRole.valueOf(request.getRole()));
        }
        if (request.getActive() != null) {
            profile.setActive(request.getActive());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            profile.setPassword(passwordEncoder.encode(request.getPassword()));
        }
    }

    public UserDto toDto(UserProfile profile) {
        return new UserDto(
                profile.getId().toString(),
                profile.getEmail(),
                profile.getFullName(),
                profile.getAvatarUrl(),
                profile.getRole().name(),
                profile.getBio(),
                profile.getPhone(),
                profile.getLocation(),
                profile.isActive(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
