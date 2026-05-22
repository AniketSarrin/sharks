package com.sharks.user.service;

import com.sharks.user.dto.RegisterRequest;
import com.sharks.user.dto.UserDto;
import com.sharks.user.entity.AdminProfile;
import com.sharks.user.entity.AttendeeProfile;
import com.sharks.user.entity.OrganizerProfile;
import com.sharks.user.entity.UserProfile;
import com.sharks.user.entity.UserRole;
import com.sharks.user.exception.EmailAlreadyExistsException;
import com.sharks.user.messaging.UserEventPublisher;
import com.sharks.user.repository.AdminProfileRepository;
import com.sharks.user.repository.AttendeeProfileRepository;
import com.sharks.user.repository.OrganizerProfileRepository;
import com.sharks.user.repository.UserProfileRepository;

import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserProfileRepository userProfileRepository;
    private final AttendeeProfileRepository attendeeProfileRepository;
    private final OrganizerProfileRepository organizerProfileRepository;
    private final AdminProfileRepository adminProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final UserEventPublisher userEventPublisher;

    public AuthService(
            UserProfileRepository userProfileRepository,
            AttendeeProfileRepository attendeeProfileRepository,
            OrganizerProfileRepository organizerProfileRepository,
            AdminProfileRepository adminProfileRepository,
            PasswordEncoder passwordEncoder,
            UserService userService,
            UserEventPublisher userEventPublisher
    ) {
        this.userProfileRepository = userProfileRepository;
        this.attendeeProfileRepository = attendeeProfileRepository;
        this.organizerProfileRepository = organizerProfileRepository;
        this.adminProfileRepository = adminProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.userEventPublisher = userEventPublisher;
    }

    /**
     * Creates a new user profile, hashes the password for local storage, publishes {@code user.created}
     * for the auth service to provision credentials. Clients obtain a JWT from the auth service login.
     */
    public UserDto register(RegisterRequest request) {
        validateRegister(request);

        String email = request.getEmail().trim();
        if (userProfileRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException(email);
        }

        UUID id = UUID.randomUUID();
        String plainPassword = request.getPassword();

        UserProfile profile = new UserProfile();
        profile.setId(id);
        profile.setEmail(email);
        profile.setFullName(request.getFullName().trim());
        profile.setPassword(passwordEncoder.encode(plainPassword));
        if (request.getRole() != null && !request.getRole().isBlank()) {
            try {
                profile.setRole(UserRole.valueOf(request.getRole().trim()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Invalid role; must be one of: attendee, organizer, admin");
            }
        }

        // Ensure the DB write succeeds before emitting user.created for auth provisioning.
        userProfileRepository.saveAndFlush(profile);

        switch (profile.getRole()) {
            case attendee -> {
                AttendeeProfile ap = new AttendeeProfile();
                ap.setId(UUID.randomUUID());
                ap.setUserId(id);
                attendeeProfileRepository.save(ap);
            }
            case organizer -> {
                OrganizerProfile op = new OrganizerProfile();
                op.setId(UUID.randomUUID());
                op.setUserId(id);
                organizerProfileRepository.save(op);
            }
            case admin -> {
                AdminProfile adminProfile = new AdminProfile();
                adminProfile.setId(UUID.randomUUID());
                adminProfile.setUserId(id);
                adminProfileRepository.save(adminProfile);
            }
        }

        userEventPublisher.publishUserCreated(id, email, plainPassword, profile.getRole().name());

        return userService.toDto(profile);
    }

    private static void validateRegister(RegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new IllegalArgumentException("Full name must not be blank");
        }
    }
}
