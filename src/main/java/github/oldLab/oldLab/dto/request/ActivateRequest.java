package github.oldLab.oldLab.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ActivateRequest {

    @NotNull(message = "email cannot be null")
    @Email(message = "email must be valid")
    private String email;

    @NotNull(message = "otp cannot be null")
    @Digits(integer = 4, fraction = 0, message = "otp must be 4 digits")
    private int otp;
}
