package github.oldLab.oldLab.serviceImpl;

import github.oldLab.oldLab.Enum.ReportStatusEnum;
import github.oldLab.oldLab.controller.FeignNotificationController;
import github.oldLab.oldLab.dto.response.ReportResponse;

import github.oldLab.oldLab.dto.response.ReviewResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationReportsServiceImpl {

    private final FeignNotificationController feignClient;
    private final CircuitBreaker circuitBreaker;

    public ReportResponse getReportById(Long reportId) {
        return circuitBreaker.executeSupplier(() ->
                feignClient.getReportById(reportId)
        );
    }

    public ReviewResponse getReviewById(Long reviewId) {
        return circuitBreaker.executeSupplier(() ->
                feignClient.getReviewById(reviewId));
    }

    public ResponseEntity<List<ReviewResponse>> getReviewsOfShopsByAuthorId(Long authorId) {
        return circuitBreaker.executeSupplier(() ->
                feignClient.getReviewsOfShopsByAuthorId(authorId)
        );
    }

    public ResponseEntity<List<ReviewResponse>> getReviewsByShopId(Long shopId, int page, int size) {
        return circuitBreaker.executeSupplier(() ->
                feignClient.getReviewsByShopId(shopId,  page, size)
        );
    }

    public ResponseEntity<List<ReviewResponse>> getReviewsByPersonId(Long personId, int page, int size) {
        return circuitBreaker.executeSupplier(() ->
                feignClient.getReviewsByPersonId(personId,  page, size)
        );
    }

    public ResponseEntity<List<ReviewResponse>> getReviewsOfPersonsByAuthorId(Long authorId) {
        return circuitBreaker.executeSupplier(() ->
                feignClient.getReviewsOfPersonsByAuthorId(authorId)
        );
    }

    public List<ReportResponse> getAllReports(int page, int size) {
        ResponseEntity<List<ReportResponse>> response = circuitBreaker.executeSupplier(() ->
                feignClient.getAllReports(page, size)
        );
        return response.getBody() != null ? response.getBody() : Collections.emptyList();
    }

    public List<ReviewResponse> getAllReviews(int page, int size) {
        ResponseEntity<List<ReviewResponse>> response = circuitBreaker.executeSupplier(() ->
                feignClient.getAllReviews(page, size)
        );
        return response.getBody() != null ? response.getBody() : Collections.emptyList();
    }

    public List<ReportResponse> getReportsByStatus(ReportStatusEnum status, int page, int size) {
        ResponseEntity<List<ReportResponse>> response = circuitBreaker.executeSupplier(() ->
                feignClient.getReportsByStatus(status, page, size)
        );
        return response.getBody() != null ? response.getBody() : Collections.emptyList();
    }
}
