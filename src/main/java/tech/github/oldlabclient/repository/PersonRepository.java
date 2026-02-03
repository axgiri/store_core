package tech.github.oldlabclient.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tech.github.oldlabclient.entity.Person;

@Repository
public interface PersonRepository extends JpaRepository<Person, UUID> {
    Optional<Person> findById(UUID id);

    boolean existsById(UUID id);

    Person getReferenceById(UUID id);
}
