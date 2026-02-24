package tech.github.storecore.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.github.storecore.client.NotificationReportsClient;
import tech.github.storecore.dto.events.ReviewMessage;
import tech.github.storecore.dto.events.ReviewMessage.ReviewPayload;
import tech.github.storecore.dto.request.ReviewRequest;
import tech.github.storecore.exception.DuplicateReviewException;
import tech.github.storecore.exception.UserNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final PersonService personService;
    private final NotificationReportsClient notificationClient;
    private final KafkaTemplate<String, ReviewMessage> reviewKafkaTemplate;

    @Value("${kafka.topic.review}")
    private String reviewTopic;

    public void createReview(UUID authorId, ReviewRequest request) {
        validateAuthorExists(authorId);
        validatePersonExists(request.getPersonId());

        if (authorId.equals(request.getPersonId())) {
            throw new IllegalArgumentException("Cannot review yourself");
        }

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
                payload,
                Instant.now()
        );

        reviewKafkaTemplate.send(reviewTopic, message);
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
