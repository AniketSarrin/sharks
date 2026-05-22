package com.gen.auth.controller;

import com.gen.auth.client.GotrueAdminUserResponse;
import com.gen.auth.dto.LoginRequest;
import com.gen.auth.dto.LoginResponse;
import com.gen.auth.dto.SignupRequest;
import com.gen.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	// TODO(REMOVE): Test-only endpoint. Production signup flows through
	// RabbitMQ (auth.user.created) consumed by AuthInboundMessageListener.
	@Deprecated(forRemoval = true)
	@PostMapping("/signup")
	@ResponseStatus(HttpStatus.CREATED)
	public GotrueAdminUserResponse signup(@Valid @RequestBody SignupRequest request) {
		return authService.createUser(request.getEmail(), request.getPassword(), request.getRole());
	}
}
