package github.oldLab.oldLab.service;

import java.util.List;

import github.oldLab.oldLab.dto.request.ReviewRequest;
import github.oldLab.oldLab.dto.response.ReviewResponse;
import github.oldLab.oldLab.entity.Review;

public interface ReviewService {
    ReviewResponse createReview(ReviewRequest reviewRequest);
    List<Review> getReviewsByShopId(Long id);

    List<Review> getReviewsByPersonId(Long id);

    List<Review> getAllReviewsPaginated();
    
    void deleteReview(Long id);
}
