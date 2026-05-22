package com.sharks.ticketing.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class JwtUtils {

	private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

	public JwtUtils(@Value("${security.oidc.discovery-url:}") String discoveryUrl)
			throws IOException {
		if (!StringUtils.hasText(discoveryUrl)) {
			this.jwtProcessor = null;
			return;
		}

		DiscoveryDocument discoveryDocument = readDiscoveryDocument(discoveryUrl, new ObjectMapper());
		String issuer = discoveryDocument.issuer();
		URL jwksUrl = new URL(discoveryDocument.jwksUri());
		JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(jwksUrl);
		Set<JWSAlgorithm> acceptedAlgorithms = new HashSet<>();
		acceptedAlgorithms.addAll(JWSAlgorithm.Family.RSA);
		acceptedAlgorithms.addAll(JWSAlgorithm.Family.EC);
		JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(acceptedAlgorithms, keySource);

		DefaultJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
		processor.setJWSKeySelector(keySelector);
		processor.setJWTClaimsSetVerifier((claims, context) -> {
			if (!StringUtils.hasText(claims.getIssuer()) || !claims.getIssuer().equals(issuer)) {
				throw new BadJWTException("Invalid token issuer");
			}
			Date expiresAt = claims.getExpirationTime();
			if (expiresAt == null || expiresAt.toInstant().isBefore(Instant.now())) {
				throw new BadJWTException("Token is expired");
			}
		});

		this.jwtProcessor = processor;
	}

	private static DiscoveryDocument readDiscoveryDocument(String discoveryUrl, ObjectMapper objectMapper) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(discoveryUrl).openConnection();
		connection.setRequestMethod("GET");
		connection.setConnectTimeout(3000);
		connection.setReadTimeout(3000);

		int status = connection.getResponseCode();
		if (status < 200 || status >= 300) {
			throw new IllegalStateException("Failed to fetch OIDC discovery document: HTTP " + status);
		}

		try (var inputStream = connection.getInputStream()) {
			JsonNode root = objectMapper.readTree(inputStream);
			String issuer = root.path("issuer").asText();
			String jwksUri = root.path("jwks_uri").asText();
			if (!StringUtils.hasText(issuer) || !StringUtils.hasText(jwksUri)) {
				throw new IllegalStateException("OIDC discovery document missing issuer or jwks_uri");
			}
			return new DiscoveryDocument(issuer, jwksUri);
		}
	}

	private record DiscoveryDocument(String issuer, String jwksUri) {
	}

	public boolean isConfigured() {
		return jwtProcessor != null;
	}

	public JWTClaimsSet parseAndVerify(String token) throws ParseException, JOSEException, BadJWTException, BadJOSEException {
		if (jwtProcessor == null) {
			throw new IllegalStateException("security.oidc.discovery-url is not configured");
		}
		SignedJWT signedJWT = SignedJWT.parse(token);
		return jwtProcessor.process(signedJWT, null);
	}

	public UUID getUserId(JWTClaimsSet claims) {
		String sub = claims.getSubject();
		if (!StringUtils.hasText(sub)) {
			throw new IllegalArgumentException("JWT missing sub claim");
		}
		return UUID.fromString(sub);
	}

	public AppRole getRole(JWTClaimsSet claims) {
		String fromUserRole;
		try {
			fromUserRole = claims.getStringClaim("user_role");
		} catch (ParseException e) {
			throw new IllegalArgumentException("JWT user_role claim is not a string", e);
		}
		if (StringUtils.hasText(fromUserRole)) {
			return AppRole.fromDbValue(fromUserRole);
		}
		Object appMeta = claims.getClaim("app_metadata");
		if (appMeta instanceof Map<?, ?> map) {
			Object role = map.get("role");
			if (role instanceof String s && StringUtils.hasText(s)) {
				return AppRole.fromDbValue(s);
			}
		}
		return null;
	}
}
