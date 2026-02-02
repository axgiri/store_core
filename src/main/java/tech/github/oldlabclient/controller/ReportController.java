package tech.github.oldlabclient.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.github.oldlabclient.dto.request.ReportRequest;
import tech.github.oldlabclient.service.ReportService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
public class ReportController {
    
    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<Void> createReport(@Valid @RequestBody ReportRequest request) {
        log.debug("received report request: reporterId={}, targetId={}, type={}", request.getReporterId(), request.getTargetId(), request.getType());
        reportService.createReport(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
