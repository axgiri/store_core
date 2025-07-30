package github.oldLab.oldLab.controller;

import github.oldLab.oldLab.Enum.ReportStatusEnum;
import github.oldLab.oldLab.dto.request.ReportRequest;
import github.oldLab.oldLab.dto.response.ReportResponse;
import github.oldLab.oldLab.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService service;

    @PostMapping("/create")
    public ResponseEntity<ReportResponse> createReport(
            @RequestBody ReportRequest request) {
        ReportResponse response = service.createReport(request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/async/create")
    public ResponseEntity<Void> createAsync(@RequestBody ReportRequest request) {
        log.debug("creating report async: {}", request);
        service.createAsync(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<ReportResponse>> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<ReportResponse> responses = service.getAllReports(page, size);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReportResponse>> getReportsByStatus(
            @PathVariable ReportStatusEnum status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<ReportResponse> responses = service.getReportsByStatus(status, page, size);
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{reportId}/status")

    public ResponseEntity<ReportResponse> updateReportStatus(
            @PathVariable Long reportId,
            @RequestParam ReportStatusEnum status,
            @RequestHeader("X-Moderator-Id") Long moderatorId) {
        ReportResponse response = service.updateReportStatus(reportId, status, moderatorId);
        return ResponseEntity.ok(response);
    }
}
