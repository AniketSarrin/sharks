package com.sharks.admin.dto;

import java.time.Instant;
import java.util.UUID;

public record AdminUserDto(
	UUID id,
	String email,
	String fullName,
	String avatarUrl,
	String role,
	String bio,
	String phone,
	String location,
	boolean active,
	Instant createdAt,
	Instant updatedAt
) {
}
