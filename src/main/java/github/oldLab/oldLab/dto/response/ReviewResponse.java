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
            .setAuthorId(review.getAuthor() != null ? review.getAuthor().getId() : null)
            .setRating(review.getRating())
            .setPersonId(review.getPerson() != null ? review.getPerson().getId() : null)
            .setShopId(review.getShop() != null ? review.getShop().getId() : null)
            .setComment(review.getComment());
    }
}
