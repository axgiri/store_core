package tech.github.storecore.dto.events;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewMessage {
    
    private Long reviewId;
    private ReviewPayload payload;
    private Instant timestamp = Instant.now();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewPayload {
        private UUID authorId;
        private Float rating;
        private UUID personId;
        private String comment;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
