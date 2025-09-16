package github.oldLab.oldLab.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
public class ResetPasswordRequest {

    private String contact;

    @JsonProperty("is_email")
    private boolean isEmail;

    @NotNull(message = "otp cannot be null")
    @Digits(integer = 4, fraction = 0, message = "otp must be 4 digits")
    private int otpReset;

    @NotNull(message = "new password cannot be null")
    @Size(min = 6, max = 32, message = "password must be between 6 and 32 characters")
    private String newPassword;
}