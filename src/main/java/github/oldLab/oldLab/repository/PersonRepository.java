package github.oldLab.oldLab.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import github.oldLab.oldLab.entity.Person;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    
    Optional<Person> findByPhoneNumber(String phoneNumber);

    Page<Person> findByCompanyId(Long companyId, Pageable pageable);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    Optional<Person> findByEmail(String email);

    Optional<Long> findIdByPhoneNumber(String phoneNumber);
}
