package github.oldLab.oldLab.seeder.factory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

import org.springframework.stereotype.Component;

import com.github.javafaker.Faker;

import github.oldLab.oldLab.entity.Person;
import github.oldLab.oldLab.entity.RefreshToken;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RefreshTokenFactory implements DataFactory<RefreshToken> {

    private final Faker faker;

    public RefreshToken create(Person person) {
        var raw = faker.internet().uuid();
        return build(person, raw);
    }

    private RefreshToken build(Person person, String rawToken) {
        return new RefreshToken()
                .setTokenHash(sha256(rawToken))
                .setPerson(person)
                .setCreatedAt(Instant.now())
                .setUpdatedAt(Instant.now())
                .setExpiresAt(Instant.now().plusSeconds(60L * 60L * 24L * 30L))
                .setRevoked(false)
                .setReplacedByTokenId(null);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    @Override
    public RefreshToken create() { // fallback
        return build(null, faker.internet().uuid());
    }
}
