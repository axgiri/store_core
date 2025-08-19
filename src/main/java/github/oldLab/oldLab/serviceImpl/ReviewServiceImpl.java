package github.oldLab.oldLab.serviceImpl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import github.oldLab.oldLab.controller.FeignNotificationController;
import github.oldLab.oldLab.dto.events.ReviewMessage;
import github.oldLab.oldLab.exception.DuplicateReviewException;
import github.oldLab.oldLab.exception.UserNotFoundException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.stereotype.Service;

import github.oldLab.oldLab.dto.request.ReviewRequest;
import github.oldLab.oldLab.dto.response.ReviewResponse;
import github.oldLab.oldLab.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    @Value("${kafka.topic.review}")
    private String reviewTopic;

    @Value("${kafka.partition.review.create}")
    private String reviewPartitionCreate;

    @Value("${kafka.partition.review.delete}")
    private String reviewPartitionDelete;

    private final PersonServiceImpl personService;
    private final ShopServiceImpl shopService;
    private final KafkaTemplate<String, ReviewMessage> kafkaTemplate;
    private final CircuitBreaker circuitBreaker;
    private final RestTemplate restTemplate;
    private final FeignNotificationController feignNotificationController;

    @Override
    public void createReviewToPerson(ReviewRequest reviewRequest) {
        log.info("creating review to person: personId={}, authorId={}", reviewRequest.getPersonId(), reviewRequest.getAuthorId());

        if (!personService.existsById(reviewRequest.getAuthorId()) && !personService.existsById(reviewRequest.getPersonId())) {
            throw new UserNotFoundException("authorId " + reviewRequest.getAuthorId() + " or personId " + reviewRequest.getPersonId() + " not found");
        }

        ResponseEntity<List<ReviewResponse>> response = feignNotificationController.getReviewsOfPersonsByAuthorId(reviewRequest.getAuthorId());
        if (response.getBody() != null) {
            boolean hasDuplicate = Optional.ofNullable(response.getBody())
            .orElseThrow(() -> new DuplicateReviewException("author has already reviewed this person")).stream()
                    .anyMatch(r -> (reviewRequest.getPersonId() != null
                        && r.getPersonId() != null
                        && r.getPersonId().equals(reviewRequest.getPersonId()))
                    );
            if (hasDuplicate) {
                throw new DuplicateReviewException("author has already reviewed this person");
            }
        }

        ReviewMessage message = new ReviewMessage();
            message.setTimestamp(Instant.now());
            message.setPayload(reviewRequest);

        kafkaTemplate.send(reviewTopic, reviewPartitionCreate, message);
    }

    @Override
    public void createReviewToShop(ReviewRequest reviewRequest) {
        log.info("creating review to shop: shopId={}, authorId={}", reviewRequest.getShopId(), reviewRequest.getAuthorId());

        if (!personService.existsById(reviewRequest.getAuthorId()) && !shopService.existsById(reviewRequest.getShopId())) {
            throw new UserNotFoundException("authorId " + reviewRequest.getAuthorId() + " or shopId " + reviewRequest.getShopId() + " not found");
        }

        ResponseEntity<List<ReviewResponse>> response = feignNotificationController.getReviewsOfShopsByAuthorId(reviewRequest.getAuthorId());
        boolean hasDuplicate = Optional.ofNullable(response.getBody())
            .orElseThrow(() -> new DuplicateReviewException("author has already reviewed this shop")).stream()
                .anyMatch(r ->
                                (reviewRequest.getShopId() != null && r.getShopId() != null &&
                                        r.getShopId().equals(reviewRequest.getShopId())));
            if (hasDuplicate) {
                throw new DuplicateReviewException("author has already reviewed this shop");
            }
        

        ReviewMessage message = new ReviewMessage();
            message.setTimestamp(Instant.now());
            message.setPayload(reviewRequest);

        kafkaTemplate.send(reviewTopic, reviewPartitionCreate, message);
    }

    @Override
    public List<ReviewResponse> getReviewsByShopId(Long id, int page, int size) {
        String url = "http://api/notifications/reviews?shopId={id}&page={page}&size={size}";
        return circuitBreaker.executeSupplier(() ->
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<ReviewResponse>>() {},
                        id, page, size
                ).getBody()
        );
    }

    @Override
    public List<ReviewResponse> getReviewsByPersonId(Long id, int page, int size) {
        String url = "http://api/notifications/reviews?personId={id}&page={page}&size={size}";
        return circuitBreaker.executeSupplier(() ->
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<ReviewResponse>>() {},
                        id, page, size
                ).getBody()
        );
    }

    @Override
    public List<ReviewResponse> getAllReviewsPaginated(int page, int size) {
        String url = "http://api/notifications/reviews?page={page}&size={size}";
        return circuitBreaker.executeSupplier(() ->
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<ReviewResponse>>() {},
                        page, size
                ).getBody()
        );
    }

    @Override
    public void deleteReview(Long id) {
        ReviewMessage message = new ReviewMessage();
            message.setReviewId(id);
        kafkaTemplate.send(reviewTopic, reviewPartitionDelete, message);
    }
}
