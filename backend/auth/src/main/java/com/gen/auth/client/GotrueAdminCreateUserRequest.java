package com.gen.auth.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.UUID;

public record GotrueAdminCreateUserRequest(
	@JsonInclude(JsonInclude.Include.NON_NULL)
	UUID id,
	String email,
	String password,
	@JsonProperty("email_confirm") boolean emailConfirm,
	@JsonProperty("app_metadata")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	Map<String, Object> appMetadata
) {
	public GotrueAdminCreateUserRequest(String email, String password, boolean emailConfirm) {
		this(null, email, password, emailConfirm, null);
	}

	public GotrueAdminCreateUserRequest(String email, String password, boolean emailConfirm, Map<String, Object> appMetadata) {
		this(null, email, password, emailConfirm, appMetadata);
	}
}
