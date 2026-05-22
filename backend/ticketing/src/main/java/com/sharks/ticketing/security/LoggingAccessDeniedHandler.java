package com.sharks.ticketing.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/**
 * Logs authorization failures (typically {@code FORBIDDEN} from method security or access rules).
 */
public class LoggingAccessDeniedHandler implements AccessDeniedHandler {

	private static final Logger log = LoggerFactory.getLogger(LoggingAccessDeniedHandler.class);

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String principal = auth != null ? String.valueOf(auth.getPrincipal()) : "anonymous";
		String name = auth != null ? auth.getName() : "anonymous";
		String authorities = auth != null ? auth.getAuthorities().toString() : "none";

		String path = request.getRequestURI();
		String query = request.getQueryString();
		String fullPath = query != null ? path + "?" + query : path;

		log.warn(
				"Security: request rejected as FORBIDDEN — method={} path={} principal={} name={} authorities={} reason={}",
				request.getMethod(), fullPath, principal, name, authorities,
				accessDeniedException.getMessage());
		log.debug("Security: access denied exception", accessDeniedException);

		response.setStatus(HttpStatus.FORBIDDEN.value());
	}
}
