package github.oldLab.oldLab.service;

/**
 * Service for caching userId extracted from JWT tokens.
 * Provides secure caching mechanism for chat microservice integration.
 */
public interface UserIdCacheService {
    
    /**
     * Cache userId extracted from JWT token
     * @param token JWT token
     * @param userId User ID from token
     */
    void cacheUserId(String token, Long userId);
    
    /**
     * Retrieve cached userId by token
     * @param token JWT token
     * @return cached userId or null if not found/expired
     */
    Long getCachedUserId(String token);
    
    /**
     * Invalidate cached userId for given token
     * @param token JWT token
     */
    void invalidateCache(String token);
    
    /**
     * Generate cache key from token
     * @param token JWT token
     * @return cache key
     */
    String generateCacheKey(String token);
}
