package github.oldLab.oldLab.entity;

import java.math.BigDecimal;
import java.util.*;

import github.oldLab.oldLab.Enum.CategoryEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "price", precision = 19, scale = 2, nullable = false)
    private BigDecimal price;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private CategoryEnum category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ElementCollection
    @CollectionTable(name = "product_tags", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "product_hidden_labels", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "label")
    private Set<String> hiddenLabels = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "product_attributes", joinColumns = @JoinColumn(name = "product_id"))
    @MapKeyColumn(name = "attr_key")
    @Column(name = "attr_value")
    private Map<String, String> attributes = new HashMap<>();
}
