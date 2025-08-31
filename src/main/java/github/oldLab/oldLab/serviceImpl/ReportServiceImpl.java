package github.oldLab.oldLab.serviceImpl;

import github.oldLab.oldLab.Enum.ReportStatusEnum;
import github.oldLab.oldLab.controller.FeignNotificationController;
import github.oldLab.oldLab.dto.events.ReportMessage;
import github.oldLab.oldLab.dto.request.ReportRequest;
import github.oldLab.oldLab.dto.response.ReportResponse;
import github.oldLab.oldLab.exception.UserNotFoundException;
import github.oldLab.oldLab.exception.ShopNotFoundException;
import github.oldLab.oldLab.service.ReportService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${kafka.topic.report}")
    private String reportTopic;

    @Value("${kafka.partition.report.create}")
    private String reportPartitionCreate;

    @Value("${kafka.partition.report.update}")
    private String reportPartitionUpdate;

    @Value("${api.service.notification-reports}")
    private String notificationReportsApiUrl;

    private final CircuitBreaker circuitBreaker;
    private final KafkaTemplate<String, ReportMessage> kafkaTemplate;
    private final RestTemplate restTemplate;
    private final PersonServiceImpl personService;
    private final ShopServiceImpl shopService;
    private final FeignNotificationController feignNotificationController;

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
                // TODO: нужна логика проверки существования ревью в другом сервисе по notificationReportsApiUrl
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
        ReportResponse response = feignNotificationController.getReportById(reportId); 
        
        /* TODO n1
         * Vanya, here you can add CompletableFuture and skip the logic of checking the response below if needed 
         * this way, the code will continue working and will only stop if it doesn't get a response from another service
         * fewer blockages to optimize logic
         */

        if(response==null) {
            throw new UserNotFoundException("Report not found");
        }

        if(!personService.existsById(moderatorId)) {
            throw new UserNotFoundException("Moderator not found");
        }

        ReportRequest request = new ReportRequest();
            request.setStatus(status);
            
        ReportMessage message = new ReportMessage();
            message.setModeratorId(moderatorId);
            message.setReportId(reportId);
            message.setPayload(request);
        kafkaTemplate.send(reportTopic, reportPartitionUpdate, message);
    }

    public List<ReportResponse> getAllReports(int page, int size) {
        String url = notificationReportsApiUrl + "/reports?page={page}&size={size}";
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
        String url = notificationReportsApiUrl + "/reports/status/{status}?page={page}&size={size}";
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
