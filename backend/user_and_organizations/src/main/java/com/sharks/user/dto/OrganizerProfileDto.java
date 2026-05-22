package com.sharks.user.dto;

import java.time.Instant;

/**
 * Combined base {@code profiles} row and {@code organizer_profiles} for API responses.
 */
public class OrganizerProfileDto {

    private String id;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String role;
    private String bio;
    private String phone;
    private String location;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private String organizerDescription;

    public OrganizerProfileDto() {
    }

    public OrganizerProfileDto(
            String id,
            String email,
            String fullName,
            String avatarUrl,
            String role,
            String bio,
            String phone,
            String location,
            boolean active,
            Instant createdAt,
            Instant updatedAt,
            String organizerDescription
    ) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.bio = bio;
        this.phone = phone;
        this.location = location;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.organizerDescription = organizerDescription;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getOrganizerDescription() {
        return organizerDescription;
    }

    public void setOrganizerDescription(String organizerDescription) {
        this.organizerDescription = organizerDescription;
    }
}
