package tech.github.storecore.security;

import java.util.UUID;

public interface OwnershipStrategy {
    boolean isOwner(UUID userId, Long resourceId);
    ResourceType supports();
}
