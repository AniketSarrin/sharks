package com.sharks.user.controller;

import com.sharks.user.dto.OrganizerProfileDto;
import com.sharks.user.dto.OrganizerProfileUpdateRequest;
import com.sharks.user.security.AuthPrincipal;
import com.sharks.user.service.OrganizerProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizers")
public class OrganizerController {

    private final OrganizerProfileService organizerProfileService;

    public OrganizerController(OrganizerProfileService organizerProfileService) {
        this.organizerProfileService = organizerProfileService;
    }

    @GetMapping("/{id}")
    public OrganizerProfileDto getProfileById(@PathVariable UUID id) {
        return organizerProfileService.getProfileById(id);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ORGANIZER')")
    public OrganizerProfileDto getProfile(@AuthenticationPrincipal AuthPrincipal principal) {
        return organizerProfileService.getProfile(principal.userId());
    }

    @PatchMapping("/me")
    @PreAuthorize("hasRole('ORGANIZER')")
    public OrganizerProfileDto updateProfile(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody OrganizerProfileUpdateRequest request
    ) {
        return organizerProfileService.updateProfile(principal.userId(), request);
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('ORGANIZER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfile(@AuthenticationPrincipal AuthPrincipal principal) {
        organizerProfileService.deleteProfile(principal.userId());
    }
}
