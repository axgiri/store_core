package tech.github.oldlabclient.search;

import tech.github.oldlabclient.dto.response.ProductResponse;

public final class ProductDocumentResponse {
    private ProductDocumentResponse() {}

    public static ProductResponse toResponse(ProductDocument productDocument) {
        return new ProductResponse(
                productDocument.getId(),
                productDocument.getName(),
                productDocument.getDescription(),
                productDocument.getPrice(),
                productDocument.getCategory(),
                productDocument.getPersonId(),
                productDocument.getTags(),
                productDocument.getHiddenLabels(),
                productDocument.getAttributes()
        );
    }
}
