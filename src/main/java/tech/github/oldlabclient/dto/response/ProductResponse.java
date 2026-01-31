package tech.github.oldlabclient.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import tech.github.oldlabclient.Enum.CategoryEnum;
import tech.github.oldlabclient.entity.Product;

public record ProductResponse(
    Long id,
    String name,
    String description,
    BigDecimal price,
    CategoryEnum category,
    UUID personId,
    List<String> tags,
    Set<String> hiddenLabels,
    Map<String, String> attributes
) {
    public static ProductResponse fromEntity(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getCategory(),
            product.getPerson() != null ? product.getPerson().getId() : null,
            product.getTags(),
            product.getHiddenLabels(),
            product.getAttributes()
        );
    }
}
