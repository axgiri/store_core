package github.oldLab.oldLab.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RefreshRequest {

    @NotNull(message = "refresh token cannot be null")
    private String refreshToken;
}
