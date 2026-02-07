package tech.github.storecore.security;

public enum UserRole {
    USER,
    MODERATOR,
    ADMIN;

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isModerator() {
        return this == MODERATOR;
    }

    public boolean isPrivileged() {
        return this == ADMIN || this == MODERATOR;
    }
}
