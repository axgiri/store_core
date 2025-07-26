package github.oldLab.oldLab.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotNull(message = "phone number cannot be null")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "phone number must be valid")
    private String phoneNumber;

    @NotNull(message = "otp cannot be null")
    @Size(min = 4, max = 4, message = "otp must be 4 digits")
    private String otp;

    @NotNull(message = "new password cannot be null")
    @Size(min = 6, max = 32, message = "password must be between 6 and 32 characters")
    private String newPassword;
}

