package github.oldLab.oldLab.serviceImpl;

import java.util.List;

import github.oldLab.oldLab.exception.DuplicateReviewException;
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

    @Override
    public ReviewResponse createReviewToPerson(ReviewRequest reviewRequest) {
        log.info("creating review to person: personId={}, authorId={}", reviewRequest.getPersonId(), reviewRequest.getAuthorId());

        if (!personRepository.existsById(reviewRequest.getAuthorId()) && !personRepository.existsById(reviewRequest.getPersonId())) {
            throw new UserNotFoundException("authorId " + reviewRequest.getAuthorId() + " or personId " + reviewRequest.getPersonId() + " not found");
        }

        if (repository.existsByShopIdAndAuthorId(reviewRequest.getPersonId(), reviewRequest.getAuthorId())) {
            throw new DuplicateReviewException("author has already reviewed this person");
        }

        var authorRef = personRepository.getReferenceById(reviewRequest.getAuthorId());

        Review review = new Review()
                .setAuthor(authorRef)
                .setComment(reviewRequest.getComment())
                .setRating(reviewRequest.getRating())
                .setPerson(personRepository.getReferenceById(reviewRequest.getPersonId()));

        return ReviewResponse.fromEntityToDto(repository.saveAndFlush(review));
    }

    @Override
    public ReviewResponse createReviewToShop(ReviewRequest reviewRequest) {
        log.info("creating review to shop: shopId={}, authorId={}", reviewRequest.getShopId(), reviewRequest.getAuthorId());

        if (!personRepository.existsById(reviewRequest.getAuthorId()) && !shopRepository.existsById(reviewRequest.getShopId())) {
            throw new UserNotFoundException("authorId " + reviewRequest.getAuthorId() + " or shopId " + reviewRequest.getShopId() + " not found");
        }

        if (repository.existsByShopIdAndAuthorId(reviewRequest.getShopId(), reviewRequest.getAuthorId())) {
                throw new DuplicateReviewException("author has already reviewed this shop");
        }

        var authorRef = personRepository.getReferenceById(reviewRequest.getAuthorId());

        Review review = new Review()
                .setAuthor(authorRef)
                .setComment(reviewRequest.getComment())
                .setRating(reviewRequest.getRating())
                .setShop(shopRepository.getReferenceById(reviewRequest.getShopId()));

        return ReviewResponse.fromEntityToDto(repository.saveAndFlush(review));
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
