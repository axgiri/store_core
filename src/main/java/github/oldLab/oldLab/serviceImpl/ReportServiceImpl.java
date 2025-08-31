package github.oldLab.oldLab.serviceImpl;

import github.oldLab.oldLab.Enum.ReportStatusEnum;
import github.oldLab.oldLab.dto.events.ReportMessage;
import github.oldLab.oldLab.dto.request.ReportRequest;
import github.oldLab.oldLab.dto.response.ReportResponse;
import github.oldLab.oldLab.dto.response.ReviewResponse;
import github.oldLab.oldLab.exception.UserNotFoundException;
import github.oldLab.oldLab.exception.ShopNotFoundException;
import github.oldLab.oldLab.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Value("${kafka.topic.report}")
    private String reportTopic;

    @Value("${kafka.partition.report.create}")
    private String reportPartitionCreate;

    @Value("${kafka.partition.report.update}")
    private String reportPartitionUpdate;

    private final KafkaTemplate<String, ReportMessage> kafkaTemplate;
    private final PersonServiceImpl personService;
    private final NotificationReportsServiceImpl notificationReportsService;
    private final ShopServiceImpl shopService;

    @Override
    public void createReport(ReportRequest request) {
        
        if(!personService.existsById(request.getReporterId())) {
            throw new UserNotFoundException("Reporter not found");
        }

        switch (request.getType()) {
            case USER: {
                if (!personService.existsById(request.getTargetId())) {
                    throw new UserNotFoundException("target user not found");
                }
                break;
            }

            case SHOP: {
                if (!shopService.existsById(request.getTargetId())) {
                    throw new ShopNotFoundException("target shop not found");
                }
                break;
            }

            case REVIEW:
                ReviewResponse review = notificationReportsService.getReviewById(request.getTargetId());
                if (review == null) {
                    throw new UserNotFoundException("target review not found");
                }
                break;

            default:
                throw new IllegalArgumentException("unexpected report type: " + request.getType());
        }

        ReportMessage event = new ReportMessage();
            event.setPayload(request);
        kafkaTemplate.send(reportTopic, reportPartitionCreate, event);
    }

    @Override
    public void updateReportStatus(Long reportId, ReportStatusEnum status, Long moderatorId) {
        CompletableFuture
                .supplyAsync(() -> notificationReportsService.getReportById(reportId))
                .thenAcceptAsync(response -> {
                    if (response == null) {
                        throw new UserNotFoundException("Report not found");
                    }

                    if (!personService.existsById(moderatorId)) {
                        throw new UserNotFoundException("Moderator not found");
                    }

                    ReportRequest request = new ReportRequest();
                    request.setStatus(status);

                    ReportMessage message = new ReportMessage();
                    message.setModeratorId(moderatorId);
                    message.setReportId(reportId);
                    message.setPayload(request);
                    kafkaTemplate.send(reportTopic, reportPartitionUpdate, message);
                }) .exceptionally(ex -> {
                    log.error("Failed to update report status for reportId={} moderatorId={}: {}",
                            reportId, moderatorId, ex.getMessage(), ex);
                    return null;
                });
    }

    public List<ReportResponse> getAllReports(int page, int size) {
        return notificationReportsService.getAllReports(page, size);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ReportResponse> getReportsByStatus(ReportStatusEnum status, int page, int size) {
        return notificationReportsService.getReportsByStatus(status, page, size);
    }
}
