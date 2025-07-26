package github.oldLab.oldLab.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import github.oldLab.oldLab.entity.Activate;

@Repository
public interface ActivateRepository extends JpaRepository<Activate, Long>{
    
    Optional<Activate> findByPhoneNumber(String phoneNumber);

    @Modifying
    @Query("UPDATE Activate a SET a.isActive = :active WHERE a.phoneNumber = :phoneNumber")
    Activate setActiveByPhoneNumber(@Param("phoneNumber") String phoneNumber,
                                    @Param("isActive") boolean active);

    Optional<Activate> findByPhoneNumberAndIsLogin(String phoneNumber, boolean isLogin);

    Optional<Activate> findByPhoneNumberAndOtpResetAndIsActive(String phoneNumber, int otp, boolean isActive);

    boolean existsByPhoneNumber(String phoneNumber);

    @Modifying
    @Query("DELETE FROM Activate a WHERE a.createdAt < :cutoffDate")
    void deleteOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

}
