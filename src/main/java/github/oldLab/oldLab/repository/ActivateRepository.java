package github.oldLab.oldLab.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import github.oldLab.oldLab.entity.Activate;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ActivateRepository extends JpaRepository<Activate, Long>{

    Optional<Activate> findTopByPhoneNumberOrderByCreatedAtDesc(String phoneNumber);

    Optional<Activate> findTopByPhoneNumberAndIsLoginOrderByCreatedAtDesc(String phoneNumber, boolean isLogin);

    @Transactional
    Optional<Activate> findByPhoneNumberAndOtpResetAndIsActive(String phoneNumber, int otp, boolean isActive);

    boolean existsByPhoneNumber(String phoneNumber);

    @Modifying
    @Query("DELETE FROM Activate a WHERE a.createdAt < :cutoffDate")
    void deleteOlderThan(@Param("cutoffDate") Instant cutoffDate);

}
