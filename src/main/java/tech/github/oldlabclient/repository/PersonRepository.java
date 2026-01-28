package tech.github.oldlabclient.repository;

import java.util.Optional;
import java.util.UUID;
import java.lang.foreign.Linker.Option;
import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import tech.github.oldlabclient.entity.Person;

import org.springframework.data.repository.query.Param;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    
    Optional<Person> findByPhoneNumber(String phoneNumber);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM Person p WHERE p.isActive = false AND p.createdAt < :cutoffDate")
    int deleteByIsActiveFalseAndCreatedAtBefore(@Param("cutoffDate") Instant cutoffDate);

    Optional<Person> findById(UUID id);

    boolean existsById(UUID id);

    Person getReferenceById(UUID id);
}
