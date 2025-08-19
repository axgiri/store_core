package github.oldLab.oldLab.controller;

import github.oldLab.oldLab.dto.response.ReportResponse;
import github.oldLab.oldLab.dto.response.ReviewResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "notification-reports", url = "${api.service.notification-reports}")
public interface FeignNotificationController {

    @GetMapping("/reports/report/{reportId}")
    ReportResponse getReportById(@PathVariable Long reportId);

    @GetMapping("/reviews/shops/author/{authorId}")
    ResponseEntity<List<ReviewResponse>> getReviewsOfShopsByAuthorId(@PathVariable Long authorId);

    @GetMapping("/reviews/persons/author/{authorId}")
    ResponseEntity<List<ReviewResponse>> getReviewsOfPersonsByAuthorId(@PathVariable Long authorId);
}
