package github.oldLab.oldLab.service;

import java.util.List;
import java.util.Map;

import github.oldLab.oldLab.dto.request.ReviewRequest;
import github.oldLab.oldLab.dto.response.ReviewResponse;

public interface ReviewService {
    void createReviewToPerson(ReviewRequest reviewRequest);

    List<ReviewResponse> getReviewsByPersonId(Long id, int page, int size);

    List<ReviewResponse> getAllReviewsPaginated(int page, int size);

    public Map<String, Object> getAvgRateByPersonId(Long id);
    void deleteReview(Long id);
}
