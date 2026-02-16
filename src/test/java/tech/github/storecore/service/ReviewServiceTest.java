package tech.github.storecore.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import tech.github.storecore.client.NotificationReportsClient;
import tech.github.storecore.dto.events.ReviewMessage;
import tech.github.storecore.dto.request.ReviewRequest;
import tech.github.storecore.exception.DuplicateReviewException;
import tech.github.storecore.exception.UserNotFoundException;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private PersonService personService;
    @Mock private NotificationReportsClient notificationClient;
    @Mock private KafkaTemplate<String, ReviewMessage> reviewKafkaTemplate;

    @InjectMocks
    private ReviewService reviewService;

    private void setTopic() {
        ReflectionTestUtils.setField(reviewService, "reviewTopic", "reviews");
    }

    private ReviewRequest reviewRequest(UUID personId) {
        var req = new ReviewRequest();
        req.setRating(4.5f);
        req.setPersonId(personId);
        req.setComment("Great seller");
        return req;
    }

    @Nested
    @DisplayName("createReview")
    class CreateReview {

        @Test
        @DisplayName("sends review message to Kafka when valid")
        void sendsMessage() {
            setTopic();
            var authorId = UUID.randomUUID();
            var personId = UUID.randomUUID();
            var request = reviewRequest(personId);

            when(personService.existsById(authorId)).thenReturn(true);
            when(personService.existsById(personId)).thenReturn(true);
            when(notificationClient.hasReviewByAuthor(personId, authorId)).thenReturn(false);

            reviewService.createReview(authorId, request);

            verify(reviewKafkaTemplate).send(any(String.class), any(ReviewMessage.class));
        }

        @Test
        @DisplayName("throws when author not found")
        void throws_whenAuthorMissing() {
            var authorId = UUID.randomUUID();
            var request = reviewRequest(UUID.randomUUID());
            when(personService.existsById(authorId)).thenReturn(false);

            assertThatThrownBy(() -> reviewService.createReview(authorId, request))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("Author");
        }

        @Test
        @DisplayName("throws when target person not found")
        void throws_whenTargetMissing() {
            var authorId = UUID.randomUUID();
            var personId = UUID.randomUUID();
            var request = reviewRequest(personId);
            when(personService.existsById(authorId)).thenReturn(true);
            when(personService.existsById(personId)).thenReturn(false);

            assertThatThrownBy(() -> reviewService.createReview(authorId, request))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("Person");
        }

        @Test
        @DisplayName("throws when trying to review yourself")
        void throws_whenSelfReview() {
            var id = UUID.randomUUID();
            var request = reviewRequest(id);
            when(personService.existsById(id)).thenReturn(true);

            assertThatThrownBy(() -> reviewService.createReview(id, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("yourself");
        }

        @Test
        @DisplayName("throws DuplicateReviewException when duplicate detected")
        void throws_whenDuplicate() {
            setTopic();
            var authorId = UUID.randomUUID();
            var personId = UUID.randomUUID();
            var request = reviewRequest(personId);

            when(personService.existsById(authorId)).thenReturn(true);
            when(personService.existsById(personId)).thenReturn(true);
            when(notificationClient.hasReviewByAuthor(personId, authorId)).thenReturn(true);

            assertThatThrownBy(() -> reviewService.createReview(authorId, request))
                    .isInstanceOf(DuplicateReviewException.class);
        }
    }
}
