package com.sharks.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sharks.admin.dto.AdminUserDto;
import com.sharks.admin.model.UserProfileEntity;
import com.sharks.admin.repository.UserProfileRepository;

@Service
public class AdminUserService {

	private final UserProfileRepository userProfileRepository;

	public AdminUserService(UserProfileRepository userProfileRepository) {
		this.userProfileRepository = userProfileRepository;
	}

	public List<AdminUserDto> getAllUsers() {
		return userProfileRepository.findAll()
			.stream()
			.map(AdminUserService::toDto)
			.toList();
	}

	private static AdminUserDto toDto(UserProfileEntity user) {
		return new AdminUserDto(
			user.getId(),
			user.getEmail(),
			user.getFullName(),
			user.getAvatarUrl(),
			user.getRole(),
			user.getBio(),
			user.getPhone(),
			user.getLocation(),
			user.isActive(),
			user.getCreatedAt(),
			user.getUpdatedAt()
		);
	}
}
