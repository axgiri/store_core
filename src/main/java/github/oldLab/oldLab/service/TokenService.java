package github.oldLab.oldLab.service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.Claims;

public interface TokenService {
    public String extractUsername(String token);

    public CompletableFuture<Claims> extractAllClaimsAsync(String token);

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    public CompletableFuture<String> generateTokenAsync(Map<String, Object> extraClaims, UserDetails userDetails);

    public CompletableFuture<String> generateToken(UserDetails userDetails);

    public boolean isTokenValid(String token, UserDetails userDetails);
}
