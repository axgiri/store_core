package github.oldLab.oldLab.serviceImpl;

import github.oldLab.oldLab.Enum.ReportStatusEnum;
import github.oldLab.oldLab.controller.FeignNotificationController;
import github.oldLab.oldLab.dto.events.ReportEvent;
import github.oldLab.oldLab.dto.request.ReportRequest;
import github.oldLab.oldLab.dto.response.ReportResponse;
import github.oldLab.oldLab.exception.UserNotFoundException;
import github.oldLab.oldLab.service.ReportService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final CircuitBreaker circuitBreaker;
    private final KafkaTemplate<String, ReportEvent> kafkaTemplate;
    private final RestTemplate restTemplate;
    private final PersonServiceImpl personService;
    private final FeignNotificationController feignNotificationController;

    @Override
    public void createReport(ReportRequest request) {
        if(!personService.existsById(request.getReporterId())) {
            throw new UserNotFoundException("Reporter not found");
        }
        ReportEvent event = new ReportEvent();
            event.setEventType("CREATE");
            event.setPayload(request);
        kafkaTemplate.send("report-events", "create", event);
    }

    @Override
    public void updateReportStatus(Long reportId, ReportStatusEnum status, Long moderatorId) {
        ReportResponse response = feignNotificationController.getReportById(reportId);
        if(response==null) {
            throw new UserNotFoundException("Report not found");
        }
        if(!personService.existsById(moderatorId)) {
            throw new UserNotFoundException("Moderator not found");
        }
        ReportRequest request = new ReportRequest();
            request.setStatus(status);
        ReportEvent event = new ReportEvent();
            event.setModeratorId(moderatorId);
            event.setReportId(reportId);
            event.setEventType("UPDATE_STATUS");
            event.setPayload(request);
        kafkaTemplate.send("report-events", "update-status", event);
    }

    public List<ReportResponse> getAllReports(int page, int size) {
        String url = "http://api/notifications/reports?page={page}&size={size}";
        return circuitBreaker.executeSupplier(() ->
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<ReportResponse>>() {},
                        page, size
                ).getBody()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<ReportResponse> getReportsByStatus(ReportStatusEnum status, int page, int size) {
        String url = "http://api/notifications/reports/status?status={status}&page={page}&size={size}";
        return circuitBreaker.executeSupplier(() ->
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<ReportResponse>>() {},
                        status, page, size
                ).getBody()
        );
    }
}
