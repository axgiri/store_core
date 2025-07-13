package github.oldLab.oldLab.dto.response;

import github.oldLab.oldLab.entity.Review;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ReviewResponse {
    
    private Long id;

    private Long version;

    private Long authorId;

    private Long rating;

    private Long personId;

    private Long shopId;

    private String comment;

    public static ReviewResponse fromEntityToDto(Review review) {
        return new ReviewResponse()
            .setId(review.getId())
            .setVersion(review.getVersion())
            .setAuthorId(review.getAuthorId())
            .setRating(review.getRating())
            .setPersonId(review.getPersonId())
            .setShopId(review.getShopId())
            .setComment(review.getComment());
    }
}
