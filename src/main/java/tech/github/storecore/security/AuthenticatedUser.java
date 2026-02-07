package tech.github.storecore.security;

import java.util.UUID;

public record AuthenticatedUser(UUID userId, UserRole role) {

    public boolean isPrivileged() {
        return role.isPrivileged();
    }
}
