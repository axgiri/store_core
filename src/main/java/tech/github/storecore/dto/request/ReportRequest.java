package tech.github.storecore.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tech.github.storecore.enumeration.ReportReasonEnum;
import tech.github.storecore.enumeration.ReportTypeEnum;

@Data
public class ReportRequest {

    @NotNull(message = "Reporter id cannot be null")
    private UUID reporterId;

    @NotNull(message = "Report type cannot be null")
    private ReportTypeEnum type;

    @NotNull(message = "Target id cannot be null")
    private UUID targetId;

    @NotNull(message = "Report reason cannot be null")
    private ReportReasonEnum reason;

    private String details;
}
