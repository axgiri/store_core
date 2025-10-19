package github.oldLab.oldLab.dto.request;

import java.time.Instant;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import github.oldLab.oldLab.Enum.RoleEnum;
import github.oldLab.oldLab.entity.Person;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PersonRequest {

    @NotNull(message = "first name cannot be null")
    @Size(min = 2, max = 32, message = "first name must be between 2 and 32 characters")
    private String firstName;

    @NotNull(message = "last name cannot be null")
    @Size(min = 2, max = 32, message = "last name must be between 2 and 32 characters")
    private String lastName;

    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "phone number must be valid")
    private String phoneNumber;

    @NotNull(message = "email cannot be null")
    @Email(message = "email must be valid")
    private String email;

    @NotNull(message = "password cannot be null")
    @Size(min = 6, max = 32, message = "password must be between 6 and 32 characters")
    private String password;

    private RoleEnum role;

    public Person toEntity() {
        return new Person()
                .setFirstName(firstName)
                .setLastName(lastName)
                .setPhoneNumber(phoneNumber)
                .setEmail(email)
                .setPassword(password)
                .setIsActive(false)
                .setIsNotBlocked(true)
                .setRoleEnum(this.role != null ? role : RoleEnum.USER)
                .setCreatedAt(Instant.now())
                .setUpdatedAt(Instant.now());
    }
}