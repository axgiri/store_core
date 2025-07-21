package github.oldLab.oldLab.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import github.oldLab.oldLab.entity.Person;
import github.oldLab.oldLab.entity.Review;
import github.oldLab.oldLab.entity.Shop;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReviewRequest {

    @NotNull(message = "authorId cannot be null")
    private Person author;

    @NotNull(message = "rating cannot be null")
    private Long rating;

    private Person person;

    private Shop shop;

    private String comment;

    public Review toEntity(){
        return new Review()
            .setAuthor(author)
            .setRating(rating)
            .setPerson(person)
            .setShop(shop)
            .setComment(comment);
    }
}

//TODO: check all annotations in dto and entities, add missing ones

