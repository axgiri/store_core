package github.oldLab.oldLab.service;

import io.github.bucket4j.Bucket;


public interface RateLimiterService {

    public Bucket newBucket(String ip);

}
