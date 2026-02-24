package tech.github.storecore.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.github.storecore.dto.request.ReportRequest;
import tech.github.storecore.security.AuthenticatedUser;
import tech.github.storecore.security.CurrentUser;
import tech.github.storecore.service.ReportService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<Void> createReport(@CurrentUser AuthenticatedUser user, @Valid @RequestBody ReportRequest request) {
        log.debug("received report request: reporterId={}, targetId={}, type={}", user.userId(), request.getTargetId(), request.getType());
        reportService.createReport(user.userId(), request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
