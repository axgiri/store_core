package tech.github.oldlabclient.dto.request;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tech.github.oldlabclient.Enum.CategoryEnum;
import tech.github.oldlabclient.entity.Person;
import tech.github.oldlabclient.entity.Product;

@Data
public class ProductRequest {

    @NotNull
    private String name;

    private String description;

    @NotNull
    private BigDecimal price;

    @NotNull
    private CategoryEnum category;

    private List<String> tags = new ArrayList<>();

    private Set<String> hiddenLabels = new HashSet<>();
    
    private Map<String, String> attributes = new HashMap<>();

    public Product toEntity(Person person) {
        return new Product()
                .setName(name)
                .setDescription(description)
                .setPrice(price)
                .setCategory(category)
                .setIsAvailable(true)
                .setPerson(person)
                .setTags(tags)
                .setHiddenLabels(hiddenLabels)
                .setAttributes(attributes);
    }
}
