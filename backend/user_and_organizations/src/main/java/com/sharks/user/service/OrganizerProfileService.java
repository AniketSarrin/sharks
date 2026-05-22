package com.sharks.user.service;

import com.sharks.user.dto.OrganizerProfileDto;
import com.sharks.user.dto.OrganizerProfileUpdateRequest;
import com.sharks.user.entity.OrganizerProfile;
import com.sharks.user.entity.UserProfile;
import com.sharks.user.entity.UserRole;
import com.sharks.user.exception.UserNotFoundException;
import com.sharks.user.messaging.UserEventPublisher;
import com.sharks.user.repository.OrganizerProfileRepository;
import com.sharks.user.repository.UserProfileRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrganizerProfileService {

    private final UserProfileRepository userProfileRepository;
    private final OrganizerProfileRepository organizerProfileRepository;
    private final UserEventPublisher userEventPublisher;

    public OrganizerProfileService(
            UserProfileRepository userProfileRepository,
            OrganizerProfileRepository organizerProfileRepository,
            UserEventPublisher userEventPublisher
    ) {
        this.userProfileRepository = userProfileRepository;
        this.organizerProfileRepository = organizerProfileRepository;
        this.userEventPublisher = userEventPublisher;
    }

    @Transactional(readOnly = true)
    public OrganizerProfileDto getProfileById(UUID userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        OrganizerProfile organizerProfile = organizerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Organizer profile not found"));
        return toDto(profile, organizerProfile);
    }

    @Transactional(readOnly = true)
    public OrganizerProfileDto getProfile(UUID userId) {
        UserProfile profile = findBaseProfile(userId);
        requireRole(profile, UserRole.organizer);
        OrganizerProfile organizerProfile = organizerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Organizer profile extension not found"));
        return toDto(profile, organizerProfile);
    }

    @Transactional
    public OrganizerProfileDto updateProfile(UUID userId, OrganizerProfileUpdateRequest request) {
        UserProfile profile = findBaseProfile(userId);
        requireRole(profile, UserRole.organizer);
        OrganizerProfile organizerProfile = organizerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Organizer profile extension not found"));

        applyBaseFields(profile, request);
        if (request.getOrganizerDescription() != null) {
            organizerProfile.setOrganizerDescription(request.getOrganizerDescription());
        }

        userProfileRepository.save(profile);
        organizerProfileRepository.save(organizerProfile);
        return toDto(profile, organizerProfile);
    }

    @Transactional
    public void deleteProfile(UUID userId) {
        UserProfile profile = findBaseProfile(userId);
        requireRole(profile, UserRole.organizer);
        OrganizerProfile organizerProfile = organizerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Organizer profile extension not found"));

        String email = profile.getEmail();
        organizerProfileRepository.delete(organizerProfile);
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

    private static void applyBaseFields(UserProfile profile, OrganizerProfileUpdateRequest request) {
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

    private static OrganizerProfileDto toDto(UserProfile profile, OrganizerProfile organizerProfile) {
        return new OrganizerProfileDto(
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
                profile.getUpdatedAt(),
                organizerProfile.getOrganizerDescription()
        );
    }
}
