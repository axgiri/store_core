package github.oldLab.oldLab.dto.response;

import github.oldLab.oldLab.entity.Person;
import github.oldLab.oldLab.entity.Review;
import github.oldLab.oldLab.entity.Shop;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ReviewResponse {
    
    private Long id;

    private Long version;

    private Person author;

    private Long rating;

    private Person person;

    private Shop shop;

    private String comment;

    public static ReviewResponse fromEntityToDto(Review review) {
        return new ReviewResponse()
            .setId(review.getId())
            .setVersion(review.getVersion())
            .setAuthor(review.getAuthor())
            .setRating(review.getRating())
            .setPerson(review.getPerson())
            .setShop(review.getShop())
            .setComment(review.getComment());
    }
}
