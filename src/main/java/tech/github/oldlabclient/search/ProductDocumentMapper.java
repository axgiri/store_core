package tech.github.oldlabclient.search;

import tech.github.oldlabclient.dto.response.ProductResponse;
import tech.github.oldlabclient.entity.Product;

public final class ProductDocumentMapper {
    private ProductDocumentMapper() {}

public static ProductDocument fromEntity(Product product) {
        ProductDocument productDocument = new ProductDocument();
        productDocument.setId(product.getId());
        productDocument.setName(product.getName());
        productDocument.setDescription(product.getDescription());
        productDocument.setPrice(product.getPrice());
        productDocument.setCategory(product.getCategory());
        productDocument.setPersonId(product.getPerson() != null ? product.getPerson().getId() : null);
        productDocument.setTags(product.getTags());
        productDocument.setHiddenLabels(product.getHiddenLabels());
        productDocument.setAttributes(product.getAttributes());
        return productDocument;
    }

    public static ProductResponse toResponse(ProductDocument d) {
        return new ProductResponse(
                d.getId(),
                d.getName(),
                d.getDescription(),
                d.getPrice(),
                d.getCategory(),
                d.getPersonId(),
                d.getTags(),
                d.getHiddenLabels(),
                d.getAttributes()
        );
    }
}
