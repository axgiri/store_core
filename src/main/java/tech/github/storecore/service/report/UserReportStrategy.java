package tech.github.storecore.service.report;

import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.github.storecore.client.NotificationReportsClient;
import tech.github.storecore.dto.request.ReportRequest;
import tech.github.storecore.enumeration.ReportTypeEnum;
import tech.github.storecore.exception.DuplicateReportException;
import tech.github.storecore.exception.UserNotFoundException;
import tech.github.storecore.service.PersonService;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserReportStrategy implements ReportStrategy {

    private final PersonService personService;
    private final NotificationReportsClient notificationClient;

    @Override
    public ReportTypeEnum getType() {
        return ReportTypeEnum.USER;
    }

    @Override
    public void validate(UUID reporterId, ReportRequest request) {
        if (reporterId.equals(request.getTargetId())) {
            throw new IllegalArgumentException("You cannot report yourself");
        }
        validateReporterExists(reporterId);
        validateTargetUserExists(request.getTargetId());
        validateNoDuplicateReport(reporterId, request);
    }

    private void validateReporterExists(UUID reporterId) {
        if (!personService.existsById(reporterId)) {
            throw new UserNotFoundException("Reporter not found with id: " + reporterId);
        }
    }

    private void validateTargetUserExists(UUID targetId) {
        if (!personService.existsById(targetId)) {
            throw new UserNotFoundException("Target user not found with id: " + targetId);
        }
    }

    private void validateNoDuplicateReport(UUID reporterId, ReportRequest request) {
        boolean isDuplicate = notificationClient.hasReportByReporter(
                reporterId,
                request.getTargetId(),
                request.getType());

        if (isDuplicate) {
            throw new DuplicateReportException("You have already submitted a report for this user");
        }
    }
}
