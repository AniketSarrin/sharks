package com.sharks.user.controller;

import com.sharks.user.dto.AdminProfileDto;
import com.sharks.user.dto.AdminProfileUpdateRequest;
import com.sharks.user.security.AuthPrincipal;
import com.sharks.user.service.AdminProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admins/me")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminProfileService adminProfileService;

    public AdminController(AdminProfileService adminProfileService) {
        this.adminProfileService = adminProfileService;
    }

    @GetMapping
    public AdminProfileDto getProfile(@AuthenticationPrincipal AuthPrincipal principal) {
        return adminProfileService.getProfile(principal.userId());
    }

    @PatchMapping
    public AdminProfileDto updateProfile(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody AdminProfileUpdateRequest request
    ) {
        return adminProfileService.updateProfile(principal.userId(), request);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfile(@AuthenticationPrincipal AuthPrincipal principal) {
        adminProfileService.deleteProfile(principal.userId());
    }
}
