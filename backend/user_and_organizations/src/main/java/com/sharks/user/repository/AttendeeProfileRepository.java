package com.sharks.user.repository;

import com.sharks.user.entity.AttendeeProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendeeProfileRepository extends JpaRepository<AttendeeProfile, UUID> {
    Optional<AttendeeProfile> findByUserId(UUID userId);
}
