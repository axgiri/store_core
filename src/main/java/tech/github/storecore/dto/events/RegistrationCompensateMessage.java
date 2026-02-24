package tech.github.storecore.dto.events;

import java.util.UUID;

public class RegistrationCompensateMessage {
    private UUID idempotencyKey;

    public UUID getIdempotencyKey() {
        return idempotencyKey;
    }
}
