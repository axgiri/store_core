package github.oldLab.oldLab.controller;

import github.oldLab.oldLab.Enum.ReportStatusEnum;
import github.oldLab.oldLab.dto.request.ReportRequest;
import github.oldLab.oldLab.dto.response.ReportResponse;
import github.oldLab.oldLab.service.ReportService;
import github.oldLab.oldLab.serviceImpl.RateLimiterServiceImpl;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService service;
    private final RateLimiterServiceImpl rateLimiterService;

    @PostMapping("/create")
    @PreAuthorize("@accessControlService.isSelf(authentication, #request.reporterId)")
    public ResponseEntity<Void> createReport(
            @RequestBody ReportRequest request,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("creating report: {}", request);
            service.createReport(request);
            return ResponseEntity.ok().build();
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @GetMapping
    @PreAuthorize("@accessControlService.isModerator(authentication) or @accessControlService.isAdmin(authentication)")
    public ResponseEntity<List<ReportResponse>> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("getting all reports page: {}, size: {}", page, size);
            List<ReportResponse> responses = service.getAllReports(page, size);
            return ResponseEntity.ok(responses);
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("@accessControlService.isModerator(authentication) or @accessControlService.isAdmin(authentication)")
    public ResponseEntity<List<ReportResponse>> getReportsByStatus(
            @PathVariable ReportStatusEnum status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("getting reports by status: {} page: {}, size: {}", status, page, size);
            List<ReportResponse> responses = service.getReportsByStatus(status, page, size);
            return ResponseEntity.ok(responses);
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }
    @GetMapping("/{Id}")
    @PreAuthorize("@accessControlService.isModerator(authentication) or @accessControlService.isAdmin(authentication)")
    public ResponseEntity<ReportResponse> getReportById(
            @PathVariable Long Id,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("getting report by id: {}", Id);
            ReportResponse response = service.getReportById(Id);
            return ResponseEntity.ok(response);
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }
    @GetMapping("/author/{authorId}")
    @PreAuthorize("@accessControlService.isSelf(authentication, #authorId) or @accessControlService.isModerator(authentication) or @accessControlService.isAdmin(authentication)")
    public ResponseEntity<List<ReportResponse>> getReportsByAuthorId(
            @PathVariable Long authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("getting report by id: {}", authorId);
            List<ReportResponse> responses = service.getReportsByAuthorId(authorId, page, size);
            return ResponseEntity.ok(responses);
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @PatchMapping("/{reportId}/status")
    @PreAuthorize("@accessControlService.isModerator(authentication) or @accessControlService.isAdmin(authentication)")
    public ResponseEntity<Void> updateReportStatus(
            @PathVariable Long reportId,
            @RequestParam ReportStatusEnum status,
            @RequestHeader("X-Moderator-Id") Long moderatorId,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("updating report {} status to {} by moderator {}", reportId, status, moderatorId);
            service.updateReportStatus(reportId, status, moderatorId);
            return ResponseEntity.ok().build();
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }
}
