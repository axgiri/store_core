package github.oldLab.oldLab.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import github.oldLab.oldLab.entity.Person;
import github.oldLab.oldLab.entity.Review;
import github.oldLab.oldLab.entity.Shop;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReviewRequest {

    @NotNull(message = "authorId cannot be null")
    private Long authorId;

    @NotNull(message = "rating cannot be null")
    private Long rating;

    private Long personId;

    private Long shopId;

    private String comment;

    public Review toEntity(){
        return new Review()
        .setAuthor(new Person().setId(authorId))
            .setRating(rating)
            .setPerson(personId != null ? new Person().setId(personId): null)
            .setShop(shopId != null ? new Shop().setId(shopId) : null)
            .setComment(comment);
    }

    @AssertTrue(message = "exactly one of personId or shopId must be provided")
    private boolean isPersonXorShop() {
        return (personId != null) ^ (shopId != null);
    }
}
