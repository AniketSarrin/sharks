package com.sharks.event.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
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
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		} catch (BadJOSEException | ParseException | JOSEException | IllegalArgumentException | IllegalStateException e) {
			log.debug("JWT authentication skipped or failed: {}", e.getMessage());
			SecurityContextHolder.clearContext();
		}

		filterChain.doFilter(request, response);
	}

	private static String parseBearerToken(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
			return header.substring(7).trim();
		}
		return null;
	}
}
