package com.sharks.user.service;

import com.sharks.user.dto.AdminProfileDto;
import com.sharks.user.dto.AdminProfileUpdateRequest;
import com.sharks.user.entity.AdminProfile;
import com.sharks.user.entity.UserProfile;
import com.sharks.user.entity.UserRole;
import com.sharks.user.exception.UserNotFoundException;
import com.sharks.user.messaging.UserEventPublisher;
import com.sharks.user.repository.AdminProfileRepository;
import com.sharks.user.repository.UserProfileRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AdminProfileService {

    private final UserProfileRepository userProfileRepository;
    private final AdminProfileRepository adminProfileRepository;
    private final UserEventPublisher userEventPublisher;

    public AdminProfileService(
            UserProfileRepository userProfileRepository,
            AdminProfileRepository adminProfileRepository,
            UserEventPublisher userEventPublisher
    ) {
        this.userProfileRepository = userProfileRepository;
        this.adminProfileRepository = adminProfileRepository;
        this.userEventPublisher = userEventPublisher;
    }

    @Transactional(readOnly = true)
    public AdminProfileDto getProfile(UUID userId) {
        UserProfile profile = findBaseProfile(userId);
        requireRole(profile, UserRole.admin);
        adminProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Admin profile extension not found"));
        return toDto(profile);
    }

    @Transactional
    public AdminProfileDto updateProfile(UUID userId, AdminProfileUpdateRequest request) {
        UserProfile profile = findBaseProfile(userId);
        requireRole(profile, UserRole.admin);
        adminProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Admin profile extension not found"));

        applyBaseFields(profile, request);
        userProfileRepository.save(profile);

        return toDto(profile);
    }

    @Transactional
    public void deleteProfile(UUID userId) {
        UserProfile profile = findBaseProfile(userId);
        requireRole(profile, UserRole.admin);
        AdminProfile adminProfile = adminProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Admin profile extension not found"));

        String email = profile.getEmail();
        adminProfileRepository.delete(adminProfile);
        userProfileRepository.delete(profile);
        userEventPublisher.publishUserDeletedForAuth(email);
    }

    private UserProfile findBaseProfile(UUID userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    private static void requireRole(UserProfile profile, UserRole expected) {
        if (profile.getRole() != expected) {
            throw new AccessDeniedException("Profile role does not match this endpoint");
        }
    }

    private static void applyBaseFields(UserProfile profile, AdminProfileUpdateRequest request) {
        if (request.getFullName() != null) {
            if (request.getFullName().isBlank()) {
                throw new IllegalArgumentException("Full name must not be blank");
            }
            profile.setFullName(request.getFullName());
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
    }

    private static AdminProfileDto toDto(UserProfile profile) {
        return new AdminProfileDto(
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
