package com.sharks.ticketing.security;

import java.util.UUID;

public record AuthPrincipal(UUID userId, AppRole role) {
}
