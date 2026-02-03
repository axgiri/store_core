package tech.github.oldlabclient.service.report;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.github.oldlabclient.client.NotificationReportsClient;
import tech.github.oldlabclient.dto.request.ReportRequest;
import tech.github.oldlabclient.enumeration.ReportTypeEnum;
import tech.github.oldlabclient.exception.DuplicateReportException;
import tech.github.oldlabclient.exception.UserNotFoundException;
import tech.github.oldlabclient.service.PersonService;

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
    public void validate(ReportRequest request) {
        validateNotSelfReport(request);
        validateReporterExists(request.getReporterId());
        validateTargetUserExists(request.getTargetId());
        validateNoDuplicateReport(request);
    }

    private void validateNotSelfReport(ReportRequest request) {
        if (request.getReporterId().equals(request.getTargetId())) {
            throw new IllegalArgumentException("You cannot report yourself");
        }
    }

    private void validateReporterExists(java.util.UUID reporterId) {
        if (!personService.existsById(reporterId)) {
            throw new UserNotFoundException("Reporter not found with id: " + reporterId);
        }
    }

    private void validateTargetUserExists(java.util.UUID targetId) {
        if (!personService.existsById(targetId)) {
            throw new UserNotFoundException("Target user not found with id: " + targetId);
        }
    }

    private void validateNoDuplicateReport(ReportRequest request) {
        boolean isDuplicate = notificationClient.hasReportByReporter(
                request.getReporterId(),
                request.getTargetId(),
                request.getType());
        
        if (isDuplicate) {
            throw new DuplicateReportException("You have already submitted a report for this user");
        }
    }
}
