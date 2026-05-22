package com.gen.auth.client;

import com.gen.auth.config.SupabaseProperties;
import com.gen.auth.exception.SupabaseAuthException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class SupabaseAuthClient {

	private final RestClient restClient;

	private final SupabaseProperties properties;

	private static final int ADMIN_USERS_PAGE_SIZE = 200;

	public SupabaseAuthClient(RestClient supabaseRestClient, SupabaseProperties properties) {
		this.restClient = supabaseRestClient;
		this.properties = properties;
	}

	/**
	 * Password grant against GoTrue: {@code POST /auth/v1/token?grant_type=password}.
	 */
	public GotrueSessionResponse signInWithPassword(String email, String password) {
		try {
			return restClient.post()
				.uri("/auth/v1/token?grant_type=password")
				.header("apikey", properties.getAnonKey())
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.body(new GotruePasswordGrantRequest(email, password))
				.retrieve()
				.body(GotrueSessionResponse.class);
		} catch (RestClientResponseException e) {
			String body = e.getResponseBodyAsString(StandardCharsets.UTF_8);
			throw new SupabaseAuthException(e.getStatusCode().value(), body);
		}
	}

	/**
	 * GoTrue admin API: {@code POST /auth/v1/admin/users} (requires service role).
	 */
	public GotrueAdminUserResponse adminCreateUser(GotrueAdminCreateUserRequest request) {
		String key = properties.getServiceRoleKey();
		try {
			return restClient.post()
				.uri("/auth/v1/admin/users")
				.header("apikey", key)
				.header("Authorization", "Bearer " + key)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.body(request)
				.retrieve()
				.body(GotrueAdminUserResponse.class);
		} catch (RestClientResponseException e) {
			String body = e.getResponseBodyAsString(StandardCharsets.UTF_8);
			throw new SupabaseAuthException(e.getStatusCode().value(), body);
		}
	}

	/**
	 * GoTrue admin API: {@code DELETE /auth/v1/admin/users/{userId}} (requires service role).
	 */
	public void adminDeleteUser(UUID userId) {
		String key = properties.getServiceRoleKey();
		try {
			restClient.delete()
				.uri("/auth/v1/admin/users/{userId}", userId)
				.header("apikey", key)
				.header("Authorization", "Bearer " + key)
				.retrieve()
				.toBodilessEntity();
		} catch (RestClientResponseException e) {
			String body = e.getResponseBodyAsString(StandardCharsets.UTF_8);
			throw new SupabaseAuthException(e.getStatusCode().value(), body);
		}
	}

	/**
	 * Resolves a Supabase auth user by email through paged admin user listing.
	 */
	public Optional<GotrueAdminUserResponse> findUserByEmail(String email) {
		int page = 1;
		while (true) {
			List<GotrueAdminUserResponse> users = listAdminUsers(page, ADMIN_USERS_PAGE_SIZE);
			if (users.isEmpty()) {
				return Optional.empty();
			}

			Optional<GotrueAdminUserResponse> matchedUser = users.stream()
				.filter(user -> user.email() != null && user.email().equalsIgnoreCase(email))
				.findFirst();
			if (matchedUser.isPresent()) {
				return matchedUser;
			}

			page++;
		}
	}

	private List<GotrueAdminUserResponse> listAdminUsers(int page, int perPage) {
		String key = properties.getServiceRoleKey();
		try {
			GotrueAdminListUsersResponse response = restClient.get()
				.uri(uriBuilder -> uriBuilder
					.path("/auth/v1/admin/users")
					.queryParam("page", page)
					.queryParam("per_page", perPage)
					.build())
				.header("apikey", key)
				.header("Authorization", "Bearer " + key)
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.body(GotrueAdminListUsersResponse.class);
			if (response == null || response.users() == null) {
				return List.of();
			}
			return response.users();
		} catch (RestClientResponseException e) {
			String body = e.getResponseBodyAsString(StandardCharsets.UTF_8);
			throw new SupabaseAuthException(e.getStatusCode().value(), body);
		}
	}
}
