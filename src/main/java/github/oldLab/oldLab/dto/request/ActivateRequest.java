package github.oldLab.oldLab.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ActivateRequest {

    @NotNull(message = "phone number cannot be null")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "phone number must be valid")
    private String phoneNumber;

    @NotNull(message = "otp cannot be null")
    @Digits(integer = 4, fraction = 0, message = "otp must be 4 digits")
    private int otp;
}
