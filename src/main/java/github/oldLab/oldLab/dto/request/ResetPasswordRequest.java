package github.oldLab.oldLab.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

@Data
public class ResetPasswordRequest {

    @NotBlank
    private String contact;  // Может быть email или телефон
    private boolean isEmail;

    @NotNull(message = "otp cannot be null")
    @Size(min = 4, max = 4, message = "otp must be 4 digits")
    private String otp;

    @NotNull(message = "new password cannot be null")
    @Size(min = 6, max = 32, message = "password must be between 6 and 32 characters")
    private String newPassword;
}

