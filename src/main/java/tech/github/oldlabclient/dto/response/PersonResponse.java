package tech.github.oldlabclient.dto.response;

import java.time.Instant;
import java.util.UUID;

import tech.github.oldlabclient.entity.Person;

public record PersonResponse(
    UUID id,
    Long version,
    String firstName,
    String lastName,
    String phoneNumber,
    Instant createdAt,
    Instant updatedAt
) {
    public static PersonResponse fromEntity(Person person) {
        return new PersonResponse(
            person.getId(),
            person.getVersion(),
            person.getFirstName(),
            person.getLastName(),
            person.getPhoneNumber(),
            person.getCreatedAt(),
            person.getUpdatedAt()
        );
    }
}
