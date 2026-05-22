package com.sharks.admin.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sharks.admin.model.UserProfileEntity;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, UUID> {
}
