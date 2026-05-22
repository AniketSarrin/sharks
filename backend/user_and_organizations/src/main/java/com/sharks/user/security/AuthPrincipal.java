package com.sharks.user.security;

import java.util.UUID;

/**
 * Authenticated user from JWT (same shape as events/ticketing services, plus optional email claim).
 */
public record AuthPrincipal(UUID userId, AppRole role, String email) {
}
