package com.gen.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "supabase")
@Validated
public class SupabaseProperties {

	@NotBlank
	private String baseUrl;

	/**
	 * Supabase anonymous (publishable) API key — sent as {@code apikey} on GoTrue requests.
	 */
	@NotBlank
	private String anonKey;

	/**
	 * Service role JWT — required for GoTrue admin routes (e.g. create user). Never expose to clients.
	 */
	@NotBlank
	private String serviceRoleKey;

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getAnonKey() {
		return anonKey;
	}

	public void setAnonKey(String anonKey) {
		this.anonKey = anonKey;
	}

	public String getServiceRoleKey() {
		return serviceRoleKey;
	}

	public void setServiceRoleKey(String serviceRoleKey) {
		this.serviceRoleKey = serviceRoleKey;
	}
}
