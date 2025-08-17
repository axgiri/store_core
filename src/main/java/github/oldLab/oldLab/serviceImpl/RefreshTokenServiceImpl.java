package github.oldLab.oldLab.serviceImpl;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import github.oldLab.oldLab.entity.Person;
import github.oldLab.oldLab.entity.RefreshToken;
import github.oldLab.oldLab.exception.InvalidTokenException;
import github.oldLab.oldLab.repository.RefreshTokenRepository;
import github.oldLab.oldLab.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final TokenHashServiceImpl tokenHashService;

    // secret is used by TokenHashService; keep property for configuration completeness if needed elsewhere
    @Value("${refresh.token.secret}")
    private String refreshTokenSecret;

    @Value("${refreshTokenTTL}")
    private int refreshExpiresDays;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional
    public String issue(Person person) {
        String raw = generateToken();
        String tokenHash = tokenHashService.hash(raw);

        RefreshToken refreshJWT = new RefreshToken();
        refreshJWT.setPerson(person);
        refreshJWT.setTokenHash(tokenHash);
        refreshJWT.setCreatedAt(Instant.now());
        refreshJWT.setUpdatedAt(Instant.now());
        refreshJWT.setExpiresAt(Instant.now().plus(refreshExpiresDays, ChronoUnit.DAYS));
        refreshJWT.setRevoked(false);
        repository.save(refreshJWT);
        return raw;
    }

    @Override
    @Transactional
    public RefreshToken rotate(String token) {
        String currentHash = tokenHashService.hash(token);

        RefreshToken currentToken = repository.findByTokenHash(currentHash)
            .orElseThrow(() -> new InvalidTokenException("refresh token not found"));

        if (currentToken.isRevoked() || currentToken.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("refresh token invalid or expired");
        }

        String nextRaw = generateToken();
        String nextHash = tokenHashService.hash(nextRaw);

        RefreshToken next = new RefreshToken();
        next.setPerson(currentToken.getPerson());
        next.setTokenHash(nextHash);
        next.setCreatedAt(Instant.now());
        next.setUpdatedAt(Instant.now());
        next.setExpiresAt(Instant.now().plus(refreshExpiresDays, ChronoUnit.DAYS));
        next.setRevoked(false);
        next = repository.save(next);

        currentToken.setRevoked(true);
        currentToken.setUpdatedAt(Instant.now());
        currentToken.setReplacedByTokenId(next.getId());
        repository.save(currentToken);

        return next;
    }

    @Override
    @Transactional
    public void revoke(String token) {
        String hash = tokenHashService.hash(token);
        RefreshToken rt = repository.findByTokenHash(hash)
            .orElseThrow(() -> new InvalidTokenException("refresh token not found"));
        rt.setRevoked(true);
        rt.setUpdatedAt(Instant.now());
        repository.save(rt);
    }

    public void revokeAllForPerson(String refreshToken) {
        Long personId = getPersonFromToken(refreshToken).getId();
        repository.findByPersonId(personId).forEach(rt -> {
            rt.setRevoked(true);
            rt.setUpdatedAt(Instant.now());
            repository.save(rt);
        });
    }

    public Person getPersonFromToken(String rawToken) {
        String hash = tokenHashService.hash(rawToken);
        RefreshToken rt = repository.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidTokenException("refresh token not found"));
        return rt.getPerson();
    }

    private String generateToken() {
        byte[] bytes = new byte[64];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public void cleanupExpiredTokens() {
        Instant cutoffDate = Instant.now().minusSeconds(60 * 60 * 24 * 180);
        repository.deleteOlderThan(cutoffDate);
    }
}
