package com.sharks.user.repository;

import com.sharks.user.entity.OrganizerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizerProfileRepository extends JpaRepository<OrganizerProfile, UUID> {
    Optional<OrganizerProfile> findByUserId(UUID userId);
}
