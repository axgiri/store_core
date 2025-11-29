package github.oldLab.oldLab.serviceImpl;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${jwt.secret.key}")
    private String KEY;

    @Value("${jwt.secret.ttl}")
    private Long TTL;
        
    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            log.error("failed to extract username from token", e);
            throw new InvalidTokenException("failed to extract username from token: " + token + "\n" + e.getMessage());
        }
    }
    
    public Claims extractAllClaims(String token) {
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token);
            return jws.getPayload();
        } catch (Exception e) {
            log.error("token validation failed: {}", e.getMessage());
            throw new InvalidTokenException("Invalid JWT token: " + e.getMessage());
        }
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = KEY.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails){
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
                    .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * TTL))
                    .signWith(getSignInKey())
                    .compact();
            return token;
        } catch (Exception e) {
            log.error("failed to generate token for user: {}", userDetails.getUsername(), e);
            throw new InvalidTokenException("Failed to generate token: " + e.getMessage());
        }
    }

    public String generateToken(UserDetails userDetails){
        return generateToken(new HashMap<>(), userDetails);
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
