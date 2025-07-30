package github.oldLab.oldLab.service;

import github.oldLab.oldLab.Enum.ReportStatusEnum;
import github.oldLab.oldLab.dto.request.ReportRequest;
import github.oldLab.oldLab.dto.response.ReportResponse;

import java.util.List;

public interface ReportService {
    public ReportResponse createReport(ReportRequest request);
    public List<ReportResponse> getAllReports(int page, int size);
    public List<ReportResponse> getReportsByStatus(ReportStatusEnum status, int page, int size);
    public ReportResponse updateReportStatus(Long reportId, ReportStatusEnum status, Long moderatorId);
    public void createAsync(ReportRequest request);
}
