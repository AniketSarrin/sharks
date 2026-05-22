package com.sharks.ticketing.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * Emits {@code UNAUTHORIZED} like {@link org.springframework.security.web.authentication.HttpStatusEntryPoint}
 * and logs why the request was rejected (filter-recorded JWT detail + entry-point exception).
 */
public class LoggingAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private static final Logger log = LoggerFactory.getLogger(LoggingAuthenticationEntryPoint.class);

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		String path = request.getRequestURI();
		String query = request.getQueryString();
		String fullPath = query != null ? path + "?" + query : path;
		String jwtDetail = AuthFailureAttributes.getReason(request);
		String remote = request.getRemoteAddr();
		String forwardedFor = request.getHeader("X-Forwarded-For");
		String userAgent = request.getHeader("User-Agent");

		String detail;
		if (jwtDetail != null) {
			detail = "jwtDetail=\"" + jwtDetail + "\"; entryPoint=" + authException.getClass().getSimpleName()
					+ ": " + nullToEmpty(authException.getMessage());
		} else {
			detail = "jwtDetail=none (anonymous request to protected resource — missing Bearer token or token was not accepted); entryPoint="
					+ authException.getClass().getSimpleName() + ": " + nullToEmpty(authException.getMessage());
		}

		log.warn(
				"Security: request rejected as UNAUTHORIZED — method={} path={} remote={} xForwardedFor={} userAgent=\"{}\" {}",
				request.getMethod(), fullPath, remote, forwardedFor, nullToEmpty(userAgent), detail);
		log.debug("Security: authentication entry point exception", authException);

		response.setStatus(HttpStatus.UNAUTHORIZED.value());
	}

	private static String nullToEmpty(String s) {
		return s != null ? s : "";
	}
}
