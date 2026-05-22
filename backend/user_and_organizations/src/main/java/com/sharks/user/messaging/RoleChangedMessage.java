package com.sharks.user.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Payload for role.changed messages from auth service.
 * Indicates that a user's role has been changed in the auth service.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RoleChangedMessage(
	String userId,
	String previousRole,
	String newRole
) {
}
