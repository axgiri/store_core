package tech.github.storecore.dto.response;

import java.time.Instant;
import java.util.UUID;

import tech.github.storecore.entity.Person;

public record PersonResponse(
    UUID id,
    String firstName,
    String lastName,
    String phoneNumber,
    Instant createdAt
) {
    public static PersonResponse fromEntity(Person person) {
        return new PersonResponse(
            person.getId(),
            person.getFirstName(),
            person.getLastName(),
            person.getPhoneNumber(),
            person.getCreatedAt()
        );
    }
}
