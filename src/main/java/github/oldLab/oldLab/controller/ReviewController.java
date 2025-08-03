package github.oldLab.oldLab.controller;

import github.oldLab.oldLab.dto.request.ReviewRequest;
import github.oldLab.oldLab.dto.response.ReviewResponse;
import github.oldLab.oldLab.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/person")
    public ResponseEntity<ReviewResponse> createReviewToPerson(@RequestBody ReviewRequest reviewRequest) {
        ReviewResponse response = reviewService.createReviewToPerson(reviewRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/shop")
    public ResponseEntity<ReviewResponse> createReviewToShop(@RequestBody ReviewRequest reviewRequest) {
        ReviewResponse response = reviewService.createReviewToShop(reviewRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByShopId(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<ReviewResponse> reviews = reviewService.getReviewsByShopId(shopId, page, size);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/person/{personId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByPersonId(
            @PathVariable Long personId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<ReviewResponse> reviews = reviewService.getReviewsByPersonId(personId, page, size);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<ReviewResponse> reviews = reviewService.getAllReviewsPaginated(page, size);
        return ResponseEntity.ok(reviews);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok().build();
    }
}