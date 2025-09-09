package github.oldLab.oldLab.seeder.factory;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

import com.github.javafaker.Faker;

import github.oldLab.oldLab.entity.Person;
import github.oldLab.oldLab.entity.Review;
import github.oldLab.oldLab.entity.Shop;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReviewFactory implements DataFactory<Review> {

    private final Faker faker;

    public Review create(Person author, Person targetPerson, Shop shop) {
        return new Review()
                .setAuthor(author)
                .setPerson(targetPerson)
                .setShop(shop)
                .setRating((long) ThreadLocalRandom.current().nextInt(1, 6))
                .setComment(faker.lorem().sentence());
    }

    @Override
    public Review create() { // fallback
        return new Review().setRating(5L).setComment("Great!");
    }
}
