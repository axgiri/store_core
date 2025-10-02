package github.oldLab.oldLab.controller;

import github.oldLab.oldLab.Enum.ReportStatusEnum;
import github.oldLab.oldLab.dto.response.ReportResponse;
import github.oldLab.oldLab.dto.response.ReviewResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "notification-service", url = "http://notification-service:8081/api/notifications")
public interface FeignNotificationController {

    @GetMapping("/reviews/avg/person/{personId}")
    ResponseEntity<Map<String, Object>> getAvgRateByPersonId(@PathVariable Long personId);

    @GetMapping("/reports/report/{reportId}")
    ReportResponse getReportById(@PathVariable Long reportId);

    @GetMapping("/reviews/review/{reviewId}")
    ReviewResponse getReviewById(@PathVariable Long reviewId);

    @GetMapping("/reviews/person/{personId}")
    ResponseEntity<List<ReviewResponse>> getReviewsByPersonId(@PathVariable Long personId,
                                        @RequestParam int page,
                                        @RequestParam int size);

    @GetMapping("/reviews/persons/author/{authorId}")
    ResponseEntity<List<ReviewResponse>> getReviewsOfPersonsByAuthorId(@PathVariable Long authorId);

    @GetMapping("/reports")
    ResponseEntity<List<ReportResponse>> getAllReports(
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );

    @GetMapping("/reviews")
    ResponseEntity<List<ReviewResponse>> getAllReviews(@RequestParam int page,
                                                       @RequestParam int size);

    @GetMapping("/reports/status/{status}")
    ResponseEntity<List<ReportResponse>> getReportsByStatus(
            @PathVariable("status") ReportStatusEnum status,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );
}
