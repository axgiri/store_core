package github.oldLab.oldLab.dto.events;

import github.oldLab.oldLab.dto.request.ReviewRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewEvent {
    private String eventType; // CREATE, UPDATE, DELETE
    private Long reviewId;
    private ReviewRequest payload;
    private Instant timestamp = Instant.now();
}
