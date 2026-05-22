package com.sharks.user.controller;

import com.sharks.user.dto.AttendeeProfileDto;
import com.sharks.user.dto.AttendeeProfileUpdateRequest;
import com.sharks.user.security.AuthPrincipal;
import com.sharks.user.service.AttendeeProfileService;
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
@RequestMapping("/api/v1/attendees/me")
@PreAuthorize("hasRole('ATTENDEE')")
public class AttendeeController {

    private final AttendeeProfileService attendeeProfileService;

    public AttendeeController(AttendeeProfileService attendeeProfileService) {
        this.attendeeProfileService = attendeeProfileService;
    }

    @GetMapping
    public AttendeeProfileDto getProfile(@AuthenticationPrincipal AuthPrincipal principal) {
        return attendeeProfileService.getProfile(principal.userId());
    }

    @PatchMapping
    public AttendeeProfileDto updateProfile(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody AttendeeProfileUpdateRequest request
    ) {
        return attendeeProfileService.updateProfile(principal.userId(), request);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfile(@AuthenticationPrincipal AuthPrincipal principal) {
        attendeeProfileService.deleteProfile(principal.userId());
    }
}
