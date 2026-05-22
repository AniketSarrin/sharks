package com.gen.auth.messaging;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserCreatedMessage(
	UUID id,
	String email,
	String password,
	String role
) {
}
