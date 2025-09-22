package github.oldLab.oldLab.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

import github.oldLab.oldLab.entity.Person;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    
    Optional<Person> findByPhoneNumber(String phoneNumber);

    Page<Person> findByCompanyId(Long companyId, Pageable pageable);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    Optional<Person> findByEmail(String email);

    @Query("SELECT p.id FROM Person p WHERE p.email = :email")
    Optional<Long> findIdByEmail(@Param("email") String email);

    @Query("select p.companyId from Person p where p.id = :personId")
    Optional<Long> findCompanyIdById(@Param("personId") Long personId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query("UPDATE Person p SET p.isActive = :isActive WHERE p.email = :email")
    int setActiveByEmail(@Param("email") String email, @Param("isActive") boolean isActive);
}
