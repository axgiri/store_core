package github.oldLab.oldLab.service;

import java.util.List;

import github.oldLab.oldLab.dto.request.ReviewRequest;
import github.oldLab.oldLab.dto.response.ReviewResponse;

public interface ReviewService {
    void createReviewToPerson(ReviewRequest reviewRequest);
    
    void createReviewToShop(ReviewRequest reviewRequest);
    
    List<ReviewResponse> getReviewsByShopId(Long id, int page, int size);

    List<ReviewResponse> getReviewsByPersonId(Long id, int page, int size);

    List<ReviewResponse> getAllReviewsPaginated(int page, int size);

    void deleteReview(Long id);
}
