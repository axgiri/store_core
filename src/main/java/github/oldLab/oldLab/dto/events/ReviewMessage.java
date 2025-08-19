package github.oldLab.oldLab.dto.events;

import java.time.Instant;

import org.hibernate.annotations.Fetch;

import github.oldLab.oldLab.dto.request.ReviewRequest;
import lombok.Data;

@Data
public class ReviewMessage {
    
    private Long reviewId;

    @Fetch(org.hibernate.annotations.FetchMode.JOIN)
    private ReviewRequest payload;

    private Instant timestamp = Instant.now();
}
