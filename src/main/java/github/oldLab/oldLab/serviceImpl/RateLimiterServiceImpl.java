package github.oldLab.oldLab.serviceImpl;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import github.oldLab.oldLab.service.RateLimiterService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

@Service
public class RateLimiterServiceImpl implements RateLimiterService {

    @Value("${rate.limiting.window.size.in.minutes}")
    private int windowSizeInMinutes;

    @Value("${rate.limiting.max.requests.per.window}")
    private int maxRequestsPerWindow;

     private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String ip) {
        return cache.computeIfAbsent(ip, this::newBucket);
    }

    public Bucket newBucket(String ip) {
        Refill refill = Refill.greedy(maxRequestsPerWindow, Duration.ofMinutes(windowSizeInMinutes));
        Bandwidth limit = Bandwidth.classic(maxRequestsPerWindow, refill);
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
