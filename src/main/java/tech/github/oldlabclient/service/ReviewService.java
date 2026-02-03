package tech.github.oldlabclient.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.github.oldlabclient.client.NotificationReportsClient;
import tech.github.oldlabclient.dto.events.ReviewMessage;
import tech.github.oldlabclient.dto.events.ReviewMessage.ReviewPayload;
import tech.github.oldlabclient.dto.request.ReviewRequest;
import tech.github.oldlabclient.exception.DuplicateReviewException;
import tech.github.oldlabclient.exception.UserNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final PersonService personService;
    private final NotificationReportsClient notificationClient;
    private final KafkaTemplate<String, ReviewMessage> reviewKafkaTemplate;

    @Value("${kafka.topic.review}")
    private String reviewTopic;

    @Value("${kafka.partition.review.create}")
    private String reviewCreatePartition;

    public void createReview(ReviewRequest request) {
        validateAuthorExists(request.getAuthorId());
        validatePersonExists(request.getPersonId());

        if (request.getAuthorId().equals(request.getPersonId())) {
            throw new IllegalArgumentException("Cannot review yourself");
        }

        UUID authorId = request.getAuthorId();
        UUID personId = request.getPersonId();

        boolean isDuplicate = notificationClient.hasReviewByAuthor(personId, authorId);
        
        if (isDuplicate) {
            log.warn("Duplicate review detected: authorId={}, personId={}", authorId, personId);
            throw new DuplicateReviewException(
                    "You have already submitted a review for this person"
            );
        }

        ReviewPayload payload = new ReviewPayload(
                authorId,
                request.getRating(),
                personId,
                request.getComment(),
                Instant.now(),
                null
        );

        ReviewMessage message = new ReviewMessage(
                null,
                payload,
                Instant.now()
        );

        reviewKafkaTemplate.send(reviewTopic, reviewCreatePartition, message);
        log.info("Review message sent to Kafka: authorId={}, personId={}", authorId, personId);
    }

    private void validateAuthorExists(UUID authorId) {
        if (!personService.existsById(authorId)) {
            throw new UserNotFoundException("Author not found with id: " + authorId);
        }
    }

    private void validatePersonExists(UUID personId) {
        if (!personService.existsById(personId)) {
            throw new UserNotFoundException("Person not found with id: " + personId);
        }
    }
}
