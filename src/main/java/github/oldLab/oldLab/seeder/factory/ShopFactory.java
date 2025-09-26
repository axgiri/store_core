package github.oldLab.oldLab.seeder.factory;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

import com.github.javafaker.Faker;

import github.oldLab.oldLab.Enum.CategoryEnum;
import github.oldLab.oldLab.entity.Person;
import github.oldLab.oldLab.entity.Shop;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ShopFactory implements DataFactory<Shop> {

    private final Faker faker;

    public Shop create(Person owner) {
        return new Shop()
                .setName(faker.company().name())
                .setAddress(faker.address().streetAddress())
                .setEmail(faker.internet().emailAddress())
                .setPhotoHeader(faker.internet().avatar())
                .setDescription(faker.lorem().sentence())
                .setCategory(List.of(CategoryEnum.values()[ThreadLocalRandom.current().nextInt(CategoryEnum.values().length)]))
                .setOwnerId(owner);
    }

    @Override
    public Shop create() { // fallback random owner not set
        return new Shop()
                .setName(faker.company().name())
                .setAddress(faker.address().streetAddress())
                .setEmail(faker.internet().emailAddress())
                .setPhotoHeader(faker.internet().avatar())
                .setDescription(faker.lorem().sentence())
                .setCategory(List.of(CategoryEnum.TABLETS));
    }
}
