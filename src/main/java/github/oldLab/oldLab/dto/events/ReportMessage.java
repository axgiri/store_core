package github.oldLab.oldLab.dto.events;

import java.time.Instant;

import org.hibernate.annotations.Fetch;

import github.oldLab.oldLab.dto.request.ReportRequest;
import lombok.Data;

@Data
public class ReportMessage {
    
    private Long reportId;
    
    @Fetch(org.hibernate.annotations.FetchMode.JOIN)
    private ReportRequest payload;

    private Long moderatorId;

    private Instant timestamp = Instant.now();

}
