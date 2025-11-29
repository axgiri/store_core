package github.oldLab.oldLab.serviceImpl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import github.oldLab.oldLab.service.UserIdCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserIdCacheServiceImpl implements UserIdCacheService {

    private static final String CACHE_PREFIX = "chat:userid:";
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Value("${chat.user-id-cache.ttl-minutes:15}")
    private int cacheExpirationMinutes;

    @Override
    public void cacheUserId(String token, Long userId) {
        if (token == null || userId == null) {
            log.warn("Attempted to cache null token or userId");
            return;
        }
        
        try {
            String cacheKey = generateCacheKey(token);
            redisTemplate.opsForValue().set(
                cacheKey, 
                userId, 
                Duration.ofMinutes(cacheExpirationMinutes)
            );
            log.debug("Cached userId {} with key: {} (TTL: {} minutes)", userId, cacheKey, cacheExpirationMinutes);
        } catch (Exception e) {
            log.error("Failed to cache userId for token", e);
        }
    }

    @Override
    public Long getCachedUserId(String token) {
        if (token == null) {
            log.warn("Attempted to get cached userId with null token");
            return null;
        }
        
        try {
            String cacheKey = generateCacheKey(token);
            Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedValue == null) {
                log.debug("Cache miss for key: {}", cacheKey);
                return null;
            }
            
            Long userId = Long.valueOf(cachedValue.toString());
            log.debug("Cache hit for key: {}, userId: {}", cacheKey, userId);
            return userId;
        } catch (Exception e) {
            log.error("Failed to retrieve cached userId for token", e);
            return null;
        }
    }

    @Override
    public void invalidateCache(String token) {
        if (token == null) {
            log.warn("Attempted to invalidate cache with null token");
            return;
        }
        
        try {
            String cacheKey = generateCacheKey(token);
            redisTemplate.delete(cacheKey);
            log.debug("Invalidated cache for key: {}", cacheKey);
        } catch (Exception e) {
            log.error("Failed to invalidate cache for token", e);
        }
    }

    @Override
    public String generateCacheKey(String token) {
        // Use SHA-256 hash of token to create secure cache key
        // This prevents token exposure in Redis and provides consistent key length
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            String encodedHash = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            return CACHE_PREFIX + encodedHash;
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            // Fallback to simple hash if SHA-256 is not available
            return CACHE_PREFIX + String.valueOf(token.hashCode());
        }
    }
}
