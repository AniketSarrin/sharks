package com.sharks.ticketing.security;

import jakarta.servlet.http.HttpServletRequest;

public final class AuthFailureAttributes {

	public static final String REASON = "com.sharks.ticketing.security.AUTH_FAILURE_REASON";

	private AuthFailureAttributes() {
	}

	public static void setReason(HttpServletRequest request, String reason) {
		request.setAttribute(REASON, reason);
	}

	public static String getReason(HttpServletRequest request) {
		Object v = request.getAttribute(REASON);
		return v instanceof String s ? s : null;
	}

	public static void clear(HttpServletRequest request) {
		request.removeAttribute(REASON);
	}
}
