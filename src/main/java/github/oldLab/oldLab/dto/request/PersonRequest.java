package github.oldLab.oldLab.dto.request;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import github.oldLab.oldLab.Enum.RoleEnum;
import github.oldLab.oldLab.entity.Person;
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

    @NotNull(message = "phone number cannot be null")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "phone number must be valid")
    private String phoneNumber;

    @NotNull(message = "password cannot be null")
    @Size(min = 6, max = 32, message = "password must be between 6 and 32 characters")
    private String password;

    private RoleEnum role;

    private boolean isActive;

    public Person toEntity() {
        return new Person()
            .setFirstName(firstName)
            .setLastName(lastName)
            .setPhoneNumber(phoneNumber)
            .setPassword(password)
            .setRoleEnum(role)
            .setCreatedAt(LocalDate.now())
            .setUpdatedAt(LocalDate.now());
    }
}

//WHEN JWT filters will be implemented 
//TODO: add companyId, because company is not connected to person yet 