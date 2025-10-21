package github.oldLab.oldLab.service;

import github.oldLab.oldLab.Enum.ReportStatusEnum;
import github.oldLab.oldLab.dto.request.ReportRequest;
import github.oldLab.oldLab.dto.response.ReportResponse;

import java.util.List;

public interface ReportService {
    ReportResponse getReportById(Long reportId);
    void createReport(ReportRequest request);
    List<ReportResponse> getAllReports(int page, int size);
    List<ReportResponse> getReportsByStatus(ReportStatusEnum status, int page, int size);
    List<ReportResponse> getReportsByAuthorId(Long authorId, int page, int size);
    void updateReportStatus(Long reportId, ReportStatusEnum status, Long moderatorId);
}
