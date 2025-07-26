package github.oldLab.oldLab.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import github.oldLab.oldLab.dto.request.ReviewRequest;
import github.oldLab.oldLab.dto.response.ReviewResponse;
import github.oldLab.oldLab.entity.Review;
import github.oldLab.oldLab.repository.ReviewRepository;
import github.oldLab.oldLab.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    
    @Qualifier("asyncExecutor")
    private final TaskExecutor taskExecutor;

    private final ReviewRepository repository;

    @Override
    public ReviewResponse createReview(ReviewRequest reviewRequest) {
        log.info("creating review for shopId: {}", reviewRequest.getShopId());
        Review review = repository.save(reviewRequest.toEntity());
        return ReviewResponse.fromEntityToDto(review);
    }

    @Override
    public List<ReviewResponse> getReviewsByShopId(Long id, int page, int size) {
        log.info("getting reviews for shopId: {}", id);
        List<ReviewResponse> reviews = repository.findByShopId(id, PageRequest.of(page, size)).getContent().stream()
            .map(ReviewResponse::fromEntityToDto)
            .toList();
        if (reviews.isEmpty()) {
            log.warn("no reviews found for shopId: {}", id);
        }
        return reviews;
    }

    @Override
    public List<ReviewResponse> getReviewsByPersonId(Long id, int page, int size) {
        log.info("getting reviews for personId: {}", id);
        List<ReviewResponse> reviews = repository.findByPersonId(id, PageRequest.of(page, size)).getContent().stream()
            .map(ReviewResponse::fromEntityToDto)
            .toList();
        if (reviews.isEmpty()) {
            log.warn("no reviews found for personId: {}", id);
        }
        return reviews;
    }

    @Override
    public List<ReviewResponse> getAllReviewsPaginated(int page, int size) {
        log.info("getting all reviews paginated");
        List<ReviewResponse> reviews = repository.findAll(PageRequest.of(page, size)).getContent().stream()
            .map(ReviewResponse::fromEntityToDto)
            .toList();
        if (reviews.isEmpty()) {
            log.warn("no reviews found");
        }
        return reviews;
    }

    @Override
    public void deleteReview(Long id) {
        log.info("deleting review with id: {}", id);
        repository.deleteById(id);
    }
}
