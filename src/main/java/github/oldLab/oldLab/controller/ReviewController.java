package github.oldLab.oldLab.controller;

import github.oldLab.oldLab.dto.request.ReviewRequest;
import github.oldLab.oldLab.dto.response.ReviewResponse;
import github.oldLab.oldLab.service.ReviewService;
import github.oldLab.oldLab.serviceImpl.RateLimiterServiceImpl;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final RateLimiterServiceImpl rateLimiterService;

    @PostMapping("/person")
    public ResponseEntity<Void> createReviewToPerson(@RequestBody ReviewRequest reviewRequest,
                                                     HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("create review to person: {}", reviewRequest);
            reviewService.createReviewToPerson(reviewRequest);
            return ResponseEntity.ok().build();
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @GetMapping("/person/{personId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByPersonId(
            @PathVariable Long personId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("get reviews by personId: {} page: {}, size: {}", personId, page, size);
            List<ReviewResponse> reviews = reviewService.getReviewsByPersonId(personId, page, size);
            return ResponseEntity.ok(reviews);
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }
    @GetMapping("/rate/person/{personId}")
    public ResponseEntity<Map<String,Object>> getReviewsByPersonId(@PathVariable Long personId,
                                                                   HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("get rate by personId: {}", personId);
            Map<String, Object> rate = reviewService.getAvgRateByPersonId(personId);
            return ResponseEntity.ok(rate);
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByAuthorId(@PathVariable Long authorId,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "20") int size,
                                                                   HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("get reviews by authorId: {}", authorId);
            List<ReviewResponse> reviews = reviewService.getReviewsByAuthorId(authorId, page, size);
            return ResponseEntity.ok(reviews);
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("get all reviews page: {}, size: {}", page, size);
            List<ReviewResponse> reviews = reviewService.getAllReviewsPaginated(page, size);
            return ResponseEntity.ok(reviews);
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@accessControlService.isReviewOwner(authentication, #id) or @accessControlService.isAdmin(authentication) or @accessControlService.isModerator(authentication)")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("delete review id: {}", id);
            reviewService.deleteReview(id);
            return ResponseEntity.ok().build();
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }
}