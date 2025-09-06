package github.oldLab.oldLab.dto.request;

import java.math.BigDecimal;
import java.util.*;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import github.oldLab.oldLab.Enum.CategoryEnum;
import github.oldLab.oldLab.entity.Product;
import github.oldLab.oldLab.entity.Shop;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
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

    public Product toEntity(Shop shop) {
        return new Product()
                .setName(name)
                .setDescription(description)
                .setPrice(price)
                .setCategory(category)
                .setShop(shop)
                .setTags(tags)
                .setHiddenLabels(hiddenLabels)
                .setAttributes(attributes);
    }
} //TODO: connect it to shop. get products by shop id and securityService check company id by
