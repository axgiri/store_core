package tech.github.storecore.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "persons", uniqueConstraints = {@UniqueConstraint(columnNames = "phone_number")})
@Accessors(chain = true)
public class Person{

    @Id
    private UUID id;

    @Version
    private Long version;

    @NotNull(message = "first name cannot be null")
    @Column(name = "first_name")
    private String firstName;

    @NotNull(message = "last name cannot be null")
    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone_number", unique = true, nullable = true)
    private String phoneNumber;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
