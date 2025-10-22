package github.oldLab.oldLab.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import github.oldLab.oldLab.entity.Activates;

import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ActivateRepository extends JpaRepository<Activates, Long>{

    Optional<Activates> findTopByEmailOrderByCreatedAtDesc(String email);

    Optional<Activates> findTopByEmailAndIsLoginOrderByCreatedAtDesc(String email, boolean isLogin);

    @Transactional
    Optional<Activates> findByEmailAndOtpResetAndIsActive(String email, int otp, boolean isActive);

    boolean existsByEmail(String email);

    @Modifying
    @Query("DELETE FROM Activates a WHERE a.createdAt < :cutoffDate")
    void deleteOlderThan(@Param("cutoffDate") Instant cutoffDate);

}
