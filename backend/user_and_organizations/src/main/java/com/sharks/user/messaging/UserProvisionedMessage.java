package com.sharks.user.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Payload for user.provisioned messages from auth service.
 * Indicates that a user has been successfully provisioned in the auth service.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserProvisionedMessage(
	String userId,
	String email,
	String role
) {
}
