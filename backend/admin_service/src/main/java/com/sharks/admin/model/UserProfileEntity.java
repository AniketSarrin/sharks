package com.sharks.admin.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "profiles")
public class UserProfileEntity {

	@Id
	private UUID id;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(name = "full_name", nullable = false)
	private String fullName;

	@Column(name = "avatar_url")
	private String avatarUrl;

	@Column(nullable = false)
	private String role;

	@Column(columnDefinition = "TEXT")
	private String bio;

	private String phone;

	private String location;

	@Column(name = "is_active", nullable = false)
	private boolean active;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public UUID getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getFullName() {
		return fullName;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public String getRole() {
		return role;
	}

	public String getBio() {
		return bio;
	}

	public String getPhone() {
		return phone;
	}

	public String getLocation() {
		return location;
	}

	public boolean isActive() {
		return active;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
