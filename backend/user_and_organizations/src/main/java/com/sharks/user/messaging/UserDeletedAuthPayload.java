package com.sharks.user.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Payload for {@code user.deleted} toward the auth service queue. Auth expects {@code email} only.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserDeletedAuthPayload(
	String email
) {
}
