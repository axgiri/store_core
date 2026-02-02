package tech.github.oldlabclient.client;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;
import tech.github.oldlabclient.enumeration.ReportTypeEnum;
import tech.github.oldlabclient.exception.ServiceCommunicationException;

@Slf4j
@Component
public class NotificationReportsClient {

    private final RestClient restClient;

    public NotificationReportsClient(@Value("${api.service.notification-reports}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public boolean hasReviewByAuthor(UUID personId, UUID authorId) {
        log.debug("Checking duplicate review: personId={}, authorId={}", personId, authorId);
        try {
            Boolean result = restClient.get()
                    .uri("/reviews/person/{personId}/author/{authorId}", personId, authorId)
                    .retrieve()
                    .body(Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Failed to check duplicate review: personId={}, authorId={}", personId, authorId, e);
            throw new ServiceCommunicationException("Failed to validate review duplicate", e);
        }
    }

    public boolean hasReportByReporter(UUID reporterId, UUID targetId, ReportTypeEnum type) {
        log.debug("Checking duplicate report: reporterId={}, targetId={}, type={}", reporterId, targetId, type);
        try {
            Boolean result = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/reports/exists")
                            .queryParam("reporterId", reporterId.toString())
                            .queryParam("targetId", targetId.toString())
                            .queryParam("type", type.name())
                            .build())
                    .retrieve()
                    .body(Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Failed to check duplicate report: reporterId={}, targetId={}, type={}", reporterId, targetId, type, e);
            throw new ServiceCommunicationException("Failed to validate report duplicate", e);
        }
    }
}
