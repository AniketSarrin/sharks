package com.sharks.ticketing.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.proc.BadJWTException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

	private final JwtUtils jwtUtils;

	public JwtAuthenticationFilter(JwtUtils jwtUtils) {
		this.jwtUtils = jwtUtils;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			String token = parseBearerToken(request);
			if (token != null && jwtUtils.isConfigured()) {
				var claims = jwtUtils.parseAndVerify(token);
				var userId = jwtUtils.getUserId(claims);
				var role = jwtUtils.getRole(claims);
				if (role == null) {
					throw new BadJOSEException("JWT missing user_role or app_metadata.role");
				}
				var principal = new AuthPrincipal(userId, role);
				var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
				var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
				AuthFailureAttributes.clear(request);
				SecurityContextHolder.getContext().setAuthentication(authentication);
				log.debug("JWT authentication succeeded: sub={} role={} path={} method={}",
						userId, role, request.getRequestURI(), request.getMethod());
			} else if (token != null) {
				String msg = "Bearer token was sent but JWT validation is not configured "
						+ "(set security.oidc.discovery-url or SUPABASE_OIDC_DISCOVERY_URL)";
				AuthFailureAttributes.setReason(request, msg);
				log.warn("JWT rejected — {} — method={} path={}", msg, request.getMethod(), request.getRequestURI());
				SecurityContextHolder.clearContext();
			} else {
				String rawAuth = request.getHeader("Authorization");
				if (StringUtils.hasText(rawAuth) && !rawAuth.startsWith("Bearer ")) {
					String msg = "Authorization header is present but not a Bearer token (scheme may be wrong)";
					AuthFailureAttributes.setReason(request, msg);
					log.warn("JWT skipped — {} — method={} path={}", msg, request.getMethod(), request.getRequestURI());
				}
			}
		} catch (BadJOSEException | ParseException | JOSEException | IllegalArgumentException
				| IllegalStateException e) {
			String summary = summarizeJwtFailure(e);
			AuthFailureAttributes.setReason(request, summary);
			log.warn("JWT authentication failed — method={} path={} reason={}",
					request.getMethod(), request.getRequestURI(), summary);
			log.debug("JWT authentication failure stack trace", e);
			SecurityContextHolder.clearContext();
		}

		filterChain.doFilter(request, response);
	}

	private static String summarizeJwtFailure(Exception e) {
		String m = e.getMessage() != null ? e.getMessage() : "(no message)";
		if (e instanceof BadJWTException) {
			return "JWT rejected (claims/validation): " + m;
		}
		if (e instanceof BadJOSEException) {
			return "JWT rejected (JOSE processing): " + m;
		}
		if (e instanceof ParseException) {
			return "JWT rejected (parse): " + m;
		}
		if (e instanceof JOSEException) {
			return "JWT rejected (signature/crypto): " + m;
		}
		if (e instanceof IllegalArgumentException) {
			return "JWT rejected (invalid claim data): " + m;
		}
		if (e instanceof IllegalStateException) {
			return "JWT rejected (configuration): " + m;
		}
		return "JWT rejected: " + e.getClass().getSimpleName() + ": " + m;
	}

	private static String parseBearerToken(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
			return header.substring(7).trim();
		}
		return null;
	}
}
