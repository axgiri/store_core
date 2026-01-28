// package tech.github.oldlabclient.dto.events;

// import java.time.Instant;

// import org.hibernate.annotations.Fetch;

// import lombok.Data;
// import tech.github.oldlabclient.dto.request.ReviewRequest;

// @Data
// public class ReviewMessage {
    
//     private Long reviewId;

//     @Fetch(org.hibernate.annotations.FetchMode.JOIN)
//     private ReviewRequest payload;

//     private Instant timestamp = Instant.now();
// }
