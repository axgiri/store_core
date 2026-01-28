package tech.github.oldlabclient.dto.request;

import java.time.Instant;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import tech.github.oldlabclient.entity.Person;

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

    public Person toEntity() {
        return new Person()
                .setFirstName(firstName)
                .setLastName(lastName)
                .setPhoneNumber(phoneNumber)
                .setCreatedAt(Instant.now())
                .setUpdatedAt(Instant.now());
    }
}