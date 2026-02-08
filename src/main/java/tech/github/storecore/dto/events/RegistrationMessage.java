package tech.github.storecore.dto.events;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationMessage {

    private RegistrationPayload payload;
    private Instant timestamp = Instant.now();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistrationPayload {
        private UUID idempotencyKey;
        private String email;
        private String password;
    }
}
