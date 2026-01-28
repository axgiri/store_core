// package tech.github.oldlabclient.dto.events;

// import java.time.Instant;

// import org.hibernate.annotations.Fetch;

// import lombok.Data;
// import tech.github.oldlabclient.dto.request.ReportRequest;

// @Data
// public class ReportMessage {
    
//     private Long reportId;
    
//     @Fetch(org.hibernate.annotations.FetchMode.JOIN)
//     private ReportRequest payload;

//     private Long moderatorId;

//     private Instant timestamp = Instant.now();

// }
