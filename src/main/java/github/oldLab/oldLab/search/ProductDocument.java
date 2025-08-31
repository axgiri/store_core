package github.oldLab.oldLab.search;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import github.oldLab.oldLab.Enum.CategoryEnum;
import lombok.Data;

@Data
@Document(indexName = "products")
public class ProductDocument {
    @Id
    private Long id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Double)
    private BigDecimal price;

    @Field(type = FieldType.Keyword)
    private CategoryEnum category;

    @Field(type = FieldType.Long)
    private Long shopId;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(type = FieldType.Keyword)
    private Set<String> hiddenLabels;

    @Field(type = FieldType.Object)
    private Map<String, String> attributes;

}
