package github.oldLab.oldLab.serviceImpl;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import github.oldLab.oldLab.exception.InvalidTokenException;
import github.oldLab.oldLab.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TokenServiceImpl implements TokenService {

    @Value("${jwt.secret}")
    private String KEY;
        
    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            log.error("failed to extract username from token", e);
            throw new InvalidTokenException("failed to extract username from token: " + token + "\n" + e.getMessage());
        }
    }
    
    @Async("asyncExecutor")
    public CompletableFuture<Claims> extractAllClaimsAsync(String token) {
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token);
            Claims claims = jws.getPayload();
            return CompletableFuture.completedFuture(claims);
        } catch (Exception e) {
            log.error("token validation failed: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = KEY.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final CompletableFuture<Claims> claim = extractAllClaimsAsync(token);
        return claim.thenApply(claimsResolver).join();
    }

    @Async("asyncExecutor")
    public CompletableFuture<String> generateTokenAsync(Map<String, Object> extraClaims, UserDetails userDetails){
        log.info("generating token for user: {}", userDetails.getUsername());
        extraClaims.put("roles", userDetails.getAuthorities().stream()
        .map(authority -> authority.getAuthority().replace("ROLE_", ""))
        .collect(Collectors.toList()));
            try {
                log.debug("extraClaims: {}", extraClaims);
                String token = Jwts.builder()
                    .claims(extraClaims)
                    .subject(userDetails.getUsername())
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15)) //15 minute expiration
                    .signWith(getSignInKey())
                    .compact();
            return CompletableFuture.completedFuture(token);
        } catch (Exception e) {
            log.info("failed to generate token for user: {}", userDetails.getUsername(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("asyncExecutor")
    public CompletableFuture<String> generateToken(UserDetails userDetails){
        return generateTokenAsync(new HashMap<>(), userDetails);
    }

    public boolean isTokenValid(String token, UserDetails userDetails){
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
