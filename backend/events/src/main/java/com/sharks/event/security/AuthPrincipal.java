package com.sharks.event.security;

import java.util.UUID;

public record AuthPrincipal(UUID userId, AppRole role) {
}
