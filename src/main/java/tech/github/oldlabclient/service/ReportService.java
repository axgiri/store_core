package tech.github.oldlabclient.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.github.oldlabclient.client.NotificationReportsClient;
import tech.github.oldlabclient.dto.events.ReportMessage;
import tech.github.oldlabclient.dto.events.ReportMessage.ReportPayload;
import tech.github.oldlabclient.dto.request.ReportRequest;
import tech.github.oldlabclient.enumeration.ReportTypeEnum;
import tech.github.oldlabclient.exception.DuplicateReportException;
import tech.github.oldlabclient.exception.UserNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final PersonService personService;
    private final NotificationReportsClient notificationClient;
    private final KafkaTemplate<String, ReportMessage> reportKafkaTemplate;

    @Value("${kafka.topic.report}")
    private String reportTopic;

    @Value("${kafka.partition.report.create}")
    private String reportCreatePartition;

    public void createReport(ReportRequest request) {
        log.info("Creating report: reporterId={}, targetId={}, type={}", request.getReporterId(), request.getTargetId(), request.getType());

        validateReporterExists(request.getReporterId());
        
        if (request.getType() == ReportTypeEnum.USER) {
            validateUserTargetExists(request.getTargetId());
        }

        UUID reporterId = request.getReporterId();
        
        boolean isDuplicate = notificationClient.hasReportByReporter(
                reporterId, 
                request.getTargetId(), 
                request.getType()
        );
        
        if (isDuplicate) {
            log.warn("Duplicate report detected: reporterId={}, targetId={}, type={}", 
                    reporterId, request.getTargetId(), request.getType());
            throw new DuplicateReportException(
                    "You have already submitted a report for this target"
            );
        }

        ReportPayload payload = new ReportPayload(
                reporterId,
                request.getType(),
                request.getTargetId(),
                null,
                request.getReason(),
                request.getDetails()
        );

        ReportMessage message = new ReportMessage(
                null,
                payload,
                null,
                Instant.now()
        );

        reportKafkaTemplate.send(reportTopic, reportCreatePartition, message);
        log.info("Report message sent to Kafka: reporterId={}, targetId={}", 
                reporterId, request.getTargetId());
    }

    private void validateReporterExists(UUID reporterId) {
        if (!personService.existsById(reporterId)) {
            throw new UserNotFoundException("Reporter not found with id: " + reporterId);
        }
    }

    private void validateUserTargetExists(UUID targetId) {
        if (!personService.existsById(targetId)) {
            throw new UserNotFoundException("Target user not found with id: " + targetId);
        }
    }
}
