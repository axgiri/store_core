package tech.github.oldlabclient.dto.events;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.github.oldlabclient.enumeration.ReportReasonEnum;
import tech.github.oldlabclient.enumeration.ReportStatusEnum;
import tech.github.oldlabclient.enumeration.ReportTypeEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportMessage {
    
    private Long reportId;
    private ReportPayload payload;
    private UUID moderatorId;
    private Instant timestamp = Instant.now();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportPayload {
        private UUID reporterId;
        private ReportTypeEnum type;
        private UUID targetId;
        private ReportStatusEnum status;
        private ReportReasonEnum reason;
        private String details;
    }
}
