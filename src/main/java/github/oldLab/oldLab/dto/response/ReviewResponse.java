package github.oldLab.oldLab.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import github.oldLab.oldLab.entity.Review;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ReviewResponse {
    
    @JsonProperty("id")
    private Long id;

    @JsonProperty("version")
    private Long version;

    @JsonProperty("author_id")
    private Long authorId;

    @JsonProperty("rating")
    private float rating;

    @JsonProperty("person_id")
    private Long personId;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    public static ReviewResponse fromEntityToDto(Review review) {
        return new ReviewResponse()
            .setId(review.getId())
            .setVersion(review.getVersion())
            .setAuthorId(review.getAuthor() != null ? review.getAuthor().getId() : null)
            .setRating(review.getRating())
            .setPersonId(review.getPerson() != null ? review.getPerson().getId() : null)
            .setComment(review.getComment());
    }
}
