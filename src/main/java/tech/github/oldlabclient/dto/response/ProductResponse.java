package tech.github.oldlabclient.dto.response;

import java.math.BigDecimal;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;
import tech.github.oldlabclient.Enum.CategoryEnum;
import tech.github.oldlabclient.entity.Product;

@Data
@Accessors(chain = true)
public class ProductResponse {
    
    @JsonProperty("id")
    private Long id;

    @JsonProperty("version")
    private Long version;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("category")
    private CategoryEnum category;

    @JsonProperty("person_id")
    private UUID personId;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("hidden_labels")
    private Set<String> hiddenLabels;
    
    @JsonProperty("attributes")
    private Map<String, String> attributes;

    public static ProductResponse fromEntityToDto(Product product) {
        return new ProductResponse()
                .setId(product.getId())
                .setVersion(product.getVersion())
                .setName(product.getName())
                .setDescription(product.getDescription())
                .setPrice(product.getPrice())
                .setCategory(product.getCategory())
                .setPersonId(product.getPerson() != null ? product.getPerson().getId() : null)
                .setTags(product.getTags())
                .setHiddenLabels(product.getHiddenLabels())
                .setAttributes(product.getAttributes());
    }
}
