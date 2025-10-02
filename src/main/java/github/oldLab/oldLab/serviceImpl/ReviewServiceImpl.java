package github.oldLab.oldLab.serviceImpl;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import github.oldLab.oldLab.dto.events.ReviewMessage;
import github.oldLab.oldLab.exception.DuplicateReviewException;
import github.oldLab.oldLab.exception.UserNotFoundException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.stereotype.Service;

import github.oldLab.oldLab.dto.request.ReviewRequest;
import github.oldLab.oldLab.dto.response.ReviewResponse;
import github.oldLab.oldLab.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    private final KafkaTemplate<String, ReviewMessage> kafkaTemplate;
    private final NotificationReportsServiceImpl notificationReportsService;

    @Override
    public void createReviewToPerson(ReviewRequest reviewRequest) {
        log.info("creating review to person: personId={}, authorId={}", reviewRequest.getPersonId(), reviewRequest.getAuthorId());

        if (!personService.existsById(reviewRequest.getPersonId()) || !personService.existsById(reviewRequest.getAuthorId())) {
            throw new UserNotFoundException("authorId " + reviewRequest.getAuthorId() + " or personId " + reviewRequest.getPersonId() + " not found");
        }

        ResponseEntity<List<ReviewResponse>> response = notificationReportsService.getReviewsOfPersonsByAuthorId(reviewRequest.getAuthorId());
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
    public Map<String, Object> getAvgRateByPersonId(Long id) {
        Map<String, Object> rate = notificationReportsService.getRateByPersonId(id).getBody();
        return rate;
    }

    @Override
    public List<ReviewResponse> getReviewsByPersonId(Long id, int page, int size) {
        return notificationReportsService.getReviewsByPersonId(id, page, size).getBody();
    }

    @Override
    public List<ReviewResponse> getAllReviewsPaginated(int page, int size) {
        return notificationReportsService.getAllReviews(page, size);
    }

    @Override
    public void deleteReview(Long id) {
        ReviewMessage message = new ReviewMessage();
            message.setReviewId(id);
        kafkaTemplate.send(reviewTopic, reviewPartitionDelete, message);
    }
}
