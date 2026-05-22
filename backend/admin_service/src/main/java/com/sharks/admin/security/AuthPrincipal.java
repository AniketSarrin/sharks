package com.sharks.admin.security;

import java.util.UUID;

public record AuthPrincipal(UUID userId, AppRole role) {
}
