package tech.github.storecore.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.github.storecore.security.AuthenticatedUser;
import tech.github.storecore.security.CurrentUser;
import tech.github.storecore.dto.request.ReviewRequest;
import tech.github.storecore.service.ReviewService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<Void> createReview(@CurrentUser AuthenticatedUser user, @Valid @RequestBody ReviewRequest request) {
        log.debug("received review request: authorId={}, personId={}", user.userId(), request.getPersonId());
        reviewService.createReview(user.userId(), request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
