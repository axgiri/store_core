package tech.github.storecore.client;

import java.util.UUID;
import java.util.function.BooleanSupplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;
import tech.github.storecore.enumeration.ReportTypeEnum;
import tech.github.storecore.exception.ServiceCommunicationException;

@Slf4j
@Component
public class NotificationReportsClient {

    private static final String REVIEWS_PATH = "/reviews";
    private static final String REPORTS_PATH = "/reports";

    private final RestClient restClient;

    public NotificationReportsClient(@Value("${api.service.notification-reports}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public boolean hasReviewByAuthor(UUID personId, UUID authorId) {
        log.debug("Checking duplicate review: personId={}, authorId={}", personId, authorId);
        return executeRequest(() -> restClient.get()
                .uri(REVIEWS_PATH + "/person/{personId}/author/{authorId}", personId, authorId)
                .retrieve()
                .body(Boolean.class),
                "Failed to validate review duplicate");
    }

    public boolean hasReportByReporter(UUID reporterId, UUID targetId, ReportTypeEnum type) {
        log.debug("Checking duplicate report: reporterId={}, targetId={}, type={}", reporterId, targetId, type);
        return executeRequest(() -> restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(REPORTS_PATH + "/exists")
                        .queryParam("reporterId", reporterId)
                        .queryParam("targetId", targetId)
                        .queryParam("type", type.name())
                        .build())
                .retrieve()
                .body(Boolean.class),
                "Failed to validate report duplicate");
    }

    public boolean reviewExistsById(UUID reviewId) {
        log.debug("Checking review exists: reviewId={}", reviewId);
        return executeRequest(() -> restClient.get()
                .uri(REVIEWS_PATH + "/exists/{reviewId}", reviewId)
                .retrieve()
                .body(Boolean.class),
                "Failed to validate review existence");
    }

    public UUID getReviewAuthorId(UUID reviewId) {
        log.debug("Getting review author: reviewId={}", reviewId);
        try {
            return restClient.get()
                    .uri(REVIEWS_PATH + "/{reviewId}/author", reviewId)
                    .retrieve()
                    .body(UUID.class);
        } catch (Exception e) {
            throw new ServiceCommunicationException("review does not exist or failed to get review author", e);
        }
    }

    private boolean executeRequest(BooleanSupplier request, String errorMessage) {
        try {
            return request.getAsBoolean();
        } catch (Exception e) {
            throw new ServiceCommunicationException(errorMessage, e);
        }
    }
}
