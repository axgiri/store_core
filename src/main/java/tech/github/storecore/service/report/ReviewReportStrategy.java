package tech.github.storecore.service.report;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import tech.github.storecore.client.NotificationReportsClient;
import tech.github.storecore.dto.request.ReportRequest;
import tech.github.storecore.enumeration.ReportTypeEnum;
import tech.github.storecore.exception.DuplicateReportException;
import tech.github.storecore.exception.ReviewNotFoundException;
import tech.github.storecore.exception.UserNotFoundException;
import tech.github.storecore.service.PersonService;

@Component
@RequiredArgsConstructor
public class ReviewReportStrategy implements ReportStrategy {

    private final PersonService personService;
    private final NotificationReportsClient notificationClient;

    @Override
    public ReportTypeEnum getType() {
        return ReportTypeEnum.REVIEW;
    }

    @Override
    public void validate(ReportRequest request) {
        UUID reporterId = request.getReporterId();
        UUID targetId = request.getTargetId();

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            Subtask<Boolean> reporterExists = scope.fork(() -> personService.existsById(reporterId));
            Subtask<Boolean> reviewExists = scope.fork(() -> notificationClient.reviewExistsById(targetId));
            Subtask<UUID> reviewAuthor = scope.fork(() -> notificationClient.getReviewAuthorId(targetId));
            Subtask<Boolean> isDuplicate = scope.fork(() -> notificationClient.hasReportByReporter(reporterId, targetId, request.getType()));

            scope.join().throwIfFailed();

            if (Boolean.FALSE.equals(reporterExists.get())) {
                throw new UserNotFoundException("Reporter not found with id: " + reporterId);
            }
            if (Boolean.FALSE.equals(reviewExists.get())) {
                throw new ReviewNotFoundException("Review not found with id: " + targetId);
            }
            if (reporterId.equals(reviewAuthor.get())) {
                throw new IllegalArgumentException("You cannot report your own review");
            }
            if (Boolean.TRUE.equals(isDuplicate.get())) {
                throw new DuplicateReportException("You have already submitted a report for this review");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Validation interrupted", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("Validation failed", cause);
        }
    }
}
