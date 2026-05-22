package com.gen.auth.service;

import java.util.UUID;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.gen.auth.client.GotrueAdminCreateUserRequest;
import com.gen.auth.client.GotrueAdminUserResponse;
import com.gen.auth.client.SupabaseAuthClient;
import com.gen.auth.dto.LoginRequest;
import com.gen.auth.dto.LoginResponse;
import com.gen.auth.exception.SupabaseAuthException;
import com.gen.auth.model.AppRole;
import com.gen.auth.model.UserRole;
import com.gen.auth.repo.UserRoleRepository;

@Service
public class AuthService {

	private final SupabaseAuthClient supabaseAuthClient;

	private final UserRoleRepository userRoleRepository;

	public AuthService(SupabaseAuthClient supabaseAuthClient, UserRoleRepository userRoleRepository) {
		this.supabaseAuthClient = supabaseAuthClient;
		this.userRoleRepository = userRoleRepository;
	}

	public LoginResponse login(LoginRequest request) {
		var session = supabaseAuthClient.signInWithPassword(request.getEmail(), request.getPassword());
		return LoginResponse.fromGotrue(session);
	}

	/**
	 * Creates a Supabase Auth user via the GoTrue admin API.
	 * <p>
	 * Email is marked confirmed in this call ({@code email_confirm: true}) so no confirmation email flow runs.
	 * 	TODO: When enabling product email verification, set {@code email_confirm} to false here and configure GoTrue mailer / templates accordingly.
	 */
	public GotrueAdminUserResponse createUser(String email, String password, AppRole role) {
		Map<String, Object> appMetadata = Map.of("role", (Object) role.getDbValue());
		var body = new GotrueAdminCreateUserRequest(email, password, true, appMetadata);
		var createdUser = supabaseAuthClient.adminCreateUser(body);
		UUID userId = UUID.fromString(createdUser.id());

		try {
			userRoleRepository.save(new UserRole(userId, role));
		} catch (RuntimeException e) {
			// Compensating action because user creation is an external side effect.
			supabaseAuthClient.adminDeleteUser(userId);
			throw new IllegalStateException("Failed to persist user role; created auth user was rolled back", e);
		}

		return createdUser;
	}

	/**
	 * Like {@link #createUser(String, String, AppRole)} but uses a predefined Supabase user id
	 * (e.g. from cross-service RabbitMQ provisioning) so auth shares the same UUID as other services.
	 */
	public GotrueAdminUserResponse createUser(UUID userId, String email, String password, AppRole role) {
		Map<String, Object> appMetadata = Map.of("role", (Object) role.getDbValue());
		var body = new GotrueAdminCreateUserRequest(userId, email, password, true, appMetadata);
		var createdUser = supabaseAuthClient.adminCreateUser(body);

		try {
			userRoleRepository.save(new UserRole(userId, role));
		} catch (RuntimeException e) {
			// Compensating action because user creation is an external side effect.
			supabaseAuthClient.adminDeleteUser(userId);
			throw new IllegalStateException("Failed to persist user role; created auth user was rolled back", e);
		}

		return createdUser;
	}

	/**
	 * Deletes the Supabase Auth user, then removes local {@code user_roles} rows.
	 * If the user is already absent in Supabase (404), local roles are still removed.
	 */
	public void deleteUser(UUID userId) {
		try {
			supabaseAuthClient.adminDeleteUser(userId);
		} catch (SupabaseAuthException e) {
			if (e.getStatusCode() != 404) {
				throw e;
			}
		}
		userRoleRepository.deleteAll(userRoleRepository.findByUserId(userId));
	}

	/**
	 * Best-effort delete for events that only provide email.
	 * If no Supabase user is found for the email, the operation is treated as a no-op.
	 */
	public void deleteUserByEmail(String email) {
		supabaseAuthClient.findUserByEmail(email)
			.ifPresent(user -> deleteUser(UUID.fromString(user.id())));
	}
}
