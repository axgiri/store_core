package github.oldLab.oldLab.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import github.oldLab.oldLab.entity.Review;
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
            .setAuthorId(authorId)
            .setRating(rating)
            .setPersonId(personId)
            .setShopId(shopId)
            .setComment(comment);
    }
}

//TODO: check all annotations in dto and entities, add missing ones

