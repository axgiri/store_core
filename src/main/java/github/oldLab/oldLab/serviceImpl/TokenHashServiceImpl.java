package github.oldLab.oldLab.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class TokenHashServiceImpl {

    @Value("${jwt.refresh.key}")
    private String key;

    private static final String HMAC_ALG = "HmacSHA256";

    public String hash(String rawToken) {
        if (rawToken == null) {
            throw new IllegalArgumentException("rawToken is null");
        }
        try {
            Mac mac = Mac.getInstance(HMAC_ALG);
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_ALG));
            byte[] h = mac.doFinal(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(h);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash refresh token", e);
        }
    }
}
