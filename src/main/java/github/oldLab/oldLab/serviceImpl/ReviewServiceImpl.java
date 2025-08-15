package github.oldLab.oldLab.serviceImpl;

import java.time.Instant;
import java.util.List;


import github.oldLab.oldLab.dto.events.ReviewEvent;
import github.oldLab.oldLab.exception.UserNotFoundException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
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

    private final PersonServiceImpl personService;
    private final ShopServiceImpl shopService;
    private final KafkaTemplate<String, ReviewEvent> kafkaTemplate;
    private final CircuitBreaker circuitBreaker;
    private final RestTemplate restTemplate;

    @Override
    public void createReviewToPerson(ReviewRequest reviewRequest) {
        log.info("creating review to person: personId={}, authorId={}", reviewRequest.getPersonId(), reviewRequest.getAuthorId());

        if (!personService.existsById(reviewRequest.getAuthorId()) && !personService.existsById(reviewRequest.getPersonId())) {
            throw new UserNotFoundException("authorId " + reviewRequest.getAuthorId() + " or personId " + reviewRequest.getPersonId() + " not found");
        }

        //if (repository.existsByShopIdAndAuthorId(reviewRequest.getPersonId(), reviewRequest.getAuthorId())) {
        //    throw new DuplicateReviewException("author has already reviewed this person");
        //}
        // Validation will be in Notification

        ReviewEvent event = new ReviewEvent();
                event.setEventType("CREATE");
                event.setTimestamp(Instant.now());
                event.setPayload(reviewRequest);
        kafkaTemplate.send("review-events", "create", event);
    }

    @Override
    public void createReviewToShop(ReviewRequest reviewRequest) {
        log.info("creating review to shop: shopId={}, authorId={}", reviewRequest.getShopId(), reviewRequest.getAuthorId());

        if (!personService.existsById(reviewRequest.getAuthorId()) && !shopService.existsById(reviewRequest.getShopId())) {
            throw new UserNotFoundException("authorId " + reviewRequest.getAuthorId() + " or shopId " + reviewRequest.getShopId() + " not found");
        }

        //if (repository.existsByShopIdAndAuthorId(reviewRequest.getShopId(), reviewRequest.getAuthorId())) {
        //        throw new DuplicateReviewException("author has already reviewed this shop");
        //}
        // Validation will be in Notification

        ReviewEvent event = new ReviewEvent();
            event.setEventType("CREATE");
            event.setTimestamp(Instant.now());
            event.setPayload(reviewRequest);

        kafkaTemplate.send("review-events", "create", event);
    }

    @Override
    public List<ReviewResponse> getReviewsByShopId(Long id, int page, int size) {
        String url = "http://api/notification/reviews?shopId={id}&page={page}&size={size}";
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
    public List<ReviewResponse> getReviewsByPersonId(Long id, int page, int size) {
        String url = "http://api/notification/reviews?personId={id}&page={page}&size={size}";
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
    public List<ReviewResponse> getAllReviewsPaginated(int page, int size) {
        String url = "http://api/notification/reviews?page={page}&size={size}";
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
        ReviewEvent event = new ReviewEvent();
            event.setEventType("DELETE");
            event.setReviewId(id);
        kafkaTemplate.send("review-events", "create", event);
    }
}
