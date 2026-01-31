package tech.github.oldlabclient.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContactRequest {

    @NotBlank(message = "email cannot be blank")
    @Email(message = "email must be valid")
    private String email;
}
