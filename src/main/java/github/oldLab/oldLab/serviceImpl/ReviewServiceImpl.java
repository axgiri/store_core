package github.oldLab.oldLab.serviceImpl;

import java.util.List;

import github.oldLab.oldLab.exception.DuplicateReviewException;
import github.oldLab.oldLab.exception.ShopNotFoundException;
import github.oldLab.oldLab.exception.UserNotFoundException;
import github.oldLab.oldLab.repository.PersonRepository;
import github.oldLab.oldLab.repository.ShopRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import github.oldLab.oldLab.dto.request.ReviewRequest;
import github.oldLab.oldLab.dto.response.ReviewResponse;
import github.oldLab.oldLab.entity.Review;
import github.oldLab.oldLab.repository.ReviewRepository;
import github.oldLab.oldLab.service.ReviewService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    
    @Qualifier("asyncExecutor")
    private final TaskExecutor taskExecutor;

    private final ReviewRepository repository;
    private final PersonRepository personRepository;
    private final ShopRepository shopRepository;

    @Transactional
    @Override
    public ReviewResponse createReview(ReviewRequest reviewRequest) {
        log.info("creating review: shopId={}, personId={}, authorId={}", reviewRequest.getShopId(), reviewRequest.getPersonId(), reviewRequest.getAuthorId());

        if (!personRepository.existsById(reviewRequest.getAuthorId())) {
            throw new UserNotFoundException("author not found with id: " + reviewRequest.getAuthorId());
        }

        var authorRef = personRepository.getReferenceById(reviewRequest.getAuthorId());

        Review review = new Review()
                .setAuthor(authorRef)
                .setComment(reviewRequest.getComment())
                .setRating(reviewRequest.getRating());

        if (reviewRequest.getShopId() != null) {
            Long shopId = reviewRequest.getShopId();

            if (!shopRepository.existsById(shopId)) {
                throw new ShopNotFoundException("shop not found with id: " + shopId);
            }

            if (repository.existsByShopIdAndAuthorId(shopId, reviewRequest.getAuthorId())) {
                throw new DuplicateReviewException("author has already reviewed this shop");
            }

            review.setShop(shopRepository.getReferenceById(shopId));
        } else {
            Long personId = reviewRequest.getPersonId();

            if (!personRepository.existsById(personId)) {
                throw new UserNotFoundException("person not found with id: " + personId);
            }

            if (repository.existsByPersonIdAndAuthorId(personId, reviewRequest.getAuthorId())) {
                throw new DuplicateReviewException("author has already reviewed this person");
            }

            review.setPerson(personRepository.getReferenceById(personId));
        }

        Review saved = repository.saveAndFlush(review);
        return ReviewResponse.fromEntityToDto(saved);
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
