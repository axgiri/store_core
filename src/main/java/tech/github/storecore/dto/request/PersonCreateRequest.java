package tech.github.storecore.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PersonCreateRequest(
    @NotNull(message = "first name cannot be null")
    @Size(min = 2, max = 32, message = "first name must be between 2 and 32 characters")
    String firstName,

    @NotNull(message = "last name cannot be null")
    @Size(min = 2, max = 32, message = "last name must be between 2 and 32 characters")
    String lastName,

    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "phone number must be valid")
    String phoneNumber,
    
    @NotNull(message = "email cannot be null")
    @Email(message = "email should be valid")
    String email,
    
    @Size(min = 6, message = "password must be at least 6 characters long")
    @NotBlank(message = "password cannot be blank")
    String password
) {}
