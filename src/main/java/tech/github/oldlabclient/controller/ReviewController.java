package tech.github.oldlabclient.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.github.oldlabclient.dto.request.ReviewRequest;
import tech.github.oldlabclient.service.ReviewService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController {
    
    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<Void> createReview(@Valid @RequestBody ReviewRequest request, @RequestHeader("X-User-Id") UUID requesterId) {
        log.debug("received review request: authorId={}, personId={}", 
        request.getAuthorId(), request.getPersonId());
        reviewService.createReview(request, requesterId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId, @RequestHeader("X-User-Id") UUID requesterId) {
        log.debug("received delete review request: reviewId={}", reviewId);
        reviewService.deleteReview(reviewId, requesterId);
        return ResponseEntity.ok().build();
    }
}
