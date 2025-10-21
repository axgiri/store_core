package github.oldLab.oldLab.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import github.oldLab.oldLab.Enum.ReportReasonEnum;
import github.oldLab.oldLab.Enum.ReportStatusEnum;
import github.oldLab.oldLab.Enum.ReportTypeEnum;
import github.oldLab.oldLab.entity.Report;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;

@Accessors(chain = true)
@Data
public class ReportResponse {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("reporter_id")
    private Long reporterId;

    @JsonProperty("reporter_name")
    private String reporterName;

    @JsonProperty("reason")
    private ReportReasonEnum reason;

    @JsonProperty("details")
    private String details;

    @JsonProperty("status")
    private ReportStatusEnum status;

    @JsonProperty("moderator_id")
    private Long moderatorId;

    @JsonProperty("moderator_name")
    private String moderatorName;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    @JsonProperty("type")
    private ReportTypeEnum type;

    @JsonProperty("target_id")
    private Long targetId;

    public static ReportResponse fromEntityToDto(Report report) {
        return new ReportResponse()
        .setId(report.getId())
        .setReporterId(report.getReporterId().getId())
        .setReporterName(report.getReporterId().getFirstName() + " " + report.getReporterId().getLastName())
        .setReason(report.getReason())
        .setDetails(report.getDetails())
        .setStatus(report.getStatus())
        .setModeratorId(report.getModerator() != null ? report.getModerator().getId() : null)
        .setModeratorName(report.getModerator() != null ?
        report.getModerator().getFirstName() + " " + report.getModerator().getLastName() : null)
        .setCreatedAt(report.getCreatedAt())
        .setUpdatedAt(report.getUpdatedAt())
        .setType(report.getType())
        .setTargetId(report.getTargetId());
    }
}
