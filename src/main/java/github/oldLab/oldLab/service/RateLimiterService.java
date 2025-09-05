package github.oldLab.oldLab.service;

import io.github.bucket4j.Bucket;


public interface RateLimiterService {

    public Bucket newBucket(String ip);
}
/*

String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        
        if (bucket.tryConsume(1)) {
            
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

 */