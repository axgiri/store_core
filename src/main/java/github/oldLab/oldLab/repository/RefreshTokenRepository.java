package github.oldLab.oldLab.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import github.oldLab.oldLab.entity.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByPersonId(Long personId);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.createdAt < :cutoffDate")
    void deleteOlderThan(Instant cutoffDate);
}
