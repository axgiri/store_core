package tech.github.oldlabclient.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActivateRequest {

    @NotNull(message = "email cannot be null")
    @Email(message = "email must be valid")
    private String email;

    @NotNull(message = "otp cannot be null")
    @Digits(integer = 4, fraction = 0, message = "otp must be 4 digits")
    private int otp;
}
