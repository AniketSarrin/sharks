package com.sharks.user.messaging;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Payload for {@code user.created} on {@code sharks.user}.
 * <p>
 * Field names and types match the auth service {@code UserCreatedMessage} so auth can deserialize
 * without any auth-side changes.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserCreatedMessage(
	UUID id,
	String email,
	String password,
	String role
) {
}
