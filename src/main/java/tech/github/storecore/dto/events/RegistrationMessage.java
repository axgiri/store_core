package tech.github.storecore.dto.events;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationMessage {
    private UUID idempotencyKey;
    private String email;
    private String password;
}
