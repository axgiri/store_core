package github.oldLab.oldLab.seeder.factory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

import com.github.javafaker.Faker;

import github.oldLab.oldLab.Enum.CategoryEnum;
import github.oldLab.oldLab.entity.Person;
import github.oldLab.oldLab.entity.Product;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductFactory implements DataFactory<Product> {

    private final Faker faker;

    public Product create(Person person) {
        var categoryValues = CategoryEnum.values();
        var category = categoryValues[ThreadLocalRandom.current().nextInt(categoryValues.length)];
        Map<String, String> attrs = new HashMap<>();
        attrs.put("color", faker.color().name());
        attrs.put("warranty", faker.number().numberBetween(6, 36) + "m");
        return new Product()
                .setName(faker.commerce().productName())
                .setDescription(faker.lorem().sentence())
                .setPrice(BigDecimal.valueOf(faker.number().randomDouble(2, 10, 5000)))
                .setCategory(category)
                .setPerson(person)
                .setTags(new ArrayList<>(List.of(faker.commerce().material(), faker.commerce().department())))
                .setHiddenLabels(new java.util.HashSet<>(List.of(faker.lorem().word())))
                .setAttributes(attrs);
    }

    @Override
    public Product create() { // fallback without relation
        return new Product()
                .setName(faker.commerce().productName())
                .setDescription(faker.lorem().sentence())
                .setPrice(BigDecimal.valueOf(faker.number().randomDouble(2, 10, 5000)))
                .setCategory(CategoryEnum.TABLETS);
    }
}
