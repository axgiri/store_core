package github.oldLab.oldLab.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Simple DTO to accept JSON body like: { "email": "user@example.com" }
 * for initiating password reset.
 */
@Data
public class ContactRequest {

    @NotBlank(message = "email cannot be blank")
    @Email(message = "email must be valid")
    private String email;
}
