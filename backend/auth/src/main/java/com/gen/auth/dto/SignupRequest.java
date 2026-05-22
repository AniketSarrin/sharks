package com.gen.auth.dto;

import com.gen.auth.model.AppRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * TEST-ONLY. Remove when the external signup flow is finalized.
 * Production user creation flows through the RabbitMQ {@code auth.user.created} consumer.
 */
@Deprecated(forRemoval = true)
public class SignupRequest {

	@NotBlank
	@Email
	private String email;

	@NotBlank
	private String password;

	@NotNull
	private AppRole role;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public AppRole getRole() {
		return role;
	}

	public void setRole(AppRole role) {
		this.role = role;
	}
}
