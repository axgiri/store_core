package github.oldLab.oldLab.dto.events;

import github.oldLab.oldLab.dto.request.ReportRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportEvent {
    private String eventType; // CREATE, UPDATE_STATUS, DELETE
    private Long reportId;
    private ReportRequest payload;
    private Long moderatorId;
    private Instant timestamp = Instant.now();

}
