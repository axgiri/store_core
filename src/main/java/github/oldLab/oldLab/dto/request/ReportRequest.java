package github.oldLab.oldLab.dto.request;

import github.oldLab.oldLab.Enum.ReportReasonEnum;
import github.oldLab.oldLab.Enum.ReportStatusEnum;
import github.oldLab.oldLab.Enum.ReportTypeEnum;
import github.oldLab.oldLab.entity.Person;
import github.oldLab.oldLab.entity.Report;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
public class ReportRequest {

    @NotNull(message = "Reporter id cannot be null")
    private Long reporterId;

    @NotNull(message = "Report type cannot be null")
    private ReportTypeEnum type;

    @NotNull(message = "Target id cannot be null")
    private Long targetId;

    @NotNull(message = "Report reason cannot be null")
    private ReportReasonEnum reason;

    private String details; // Must be if reason == OTHER

    public Report toEntity(Person reporter) {
        return new Report()
                .setReporterId(reporter)
                .setReason(this.reason)
                .setDetails(this.details)
                .setStatus(ReportStatusEnum.PENDING)
                .setType(this.type)
                .setTargetId(this.targetId)
                .setCreatedAt(Instant.now());
    }
}
