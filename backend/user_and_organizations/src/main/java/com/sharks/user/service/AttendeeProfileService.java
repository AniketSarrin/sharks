package com.sharks.user.service;

import com.sharks.user.dto.AttendeeProfileDto;
import com.sharks.user.dto.AttendeeProfileUpdateRequest;
import com.sharks.user.entity.AttendeeProfile;
import com.sharks.user.entity.UserProfile;
import com.sharks.user.entity.UserRole;
import com.sharks.user.exception.UserNotFoundException;
import com.sharks.user.messaging.UserEventPublisher;
import com.sharks.user.repository.AttendeeProfileRepository;
import com.sharks.user.repository.UserProfileRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AttendeeProfileService {

    private final UserProfileRepository userProfileRepository;
    private final AttendeeProfileRepository attendeeProfileRepository;
    private final UserEventPublisher userEventPublisher;

    public AttendeeProfileService(
            UserProfileRepository userProfileRepository,
            AttendeeProfileRepository attendeeProfileRepository,
            UserEventPublisher userEventPublisher
    ) {
        this.userProfileRepository = userProfileRepository;
        this.attendeeProfileRepository = attendeeProfileRepository;
        this.userEventPublisher = userEventPublisher;
    }

    @Transactional(readOnly = true)
    public AttendeeProfileDto getProfile(UUID userId) {
        UserProfile profile = findBaseProfile(userId);
        requireRole(profile, UserRole.attendee);
        AttendeeProfile attendeeProfile = attendeeProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Attendee profile extension not found"));
        return toDto(profile, attendeeProfile);
    }

    @Transactional
    public AttendeeProfileDto updateProfile(UUID userId, AttendeeProfileUpdateRequest request) {
        UserProfile profile = findBaseProfile(userId);
        requireRole(profile, UserRole.attendee);
        AttendeeProfile attendeeProfile = attendeeProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Attendee profile extension not found"));

        applyBaseFields(profile, request);
        if (request.getNickname() != null) {
            attendeeProfile.setNickname(request.getNickname());
        }

        userProfileRepository.save(profile);
        attendeeProfileRepository.save(attendeeProfile);
        return toDto(profile, attendeeProfile);
    }

    @Transactional
    public void deleteProfile(UUID userId) {
        UserProfile profile = findBaseProfile(userId);
        requireRole(profile, UserRole.attendee);
        AttendeeProfile attendeeProfile = attendeeProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Attendee profile extension not found"));

        String email = profile.getEmail();
        attendeeProfileRepository.delete(attendeeProfile);
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

    private static void applyBaseFields(UserProfile profile, AttendeeProfileUpdateRequest request) {
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

    private static AttendeeProfileDto toDto(UserProfile profile, AttendeeProfile attendeeProfile) {
        return new AttendeeProfileDto(
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
                attendeeProfile.getNickname()
        );
    }
}
