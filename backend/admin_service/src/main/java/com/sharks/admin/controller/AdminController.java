package com.sharks.admin.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sharks.admin.dto.AdminUserDto;
import com.sharks.admin.service.AdminUserService;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

	private final AdminUserService adminUserService;

	public AdminController(AdminUserService adminUserService) {
		this.adminUserService = adminUserService;
	}

	@GetMapping("/users")
	public List<AdminUserDto> getAllUsers() {
		return adminUserService.getAllUsers();
	}
}
