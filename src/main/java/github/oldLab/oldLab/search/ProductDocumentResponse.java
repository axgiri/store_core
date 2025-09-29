package github.oldLab.oldLab.search;

import github.oldLab.oldLab.dto.response.ProductResponse;

public final class ProductDocumentResponse {
    private ProductDocumentResponse() {}

    public static ProductResponse toResponse(ProductDocument productDocument) {
        return new ProductResponse()
                .setId(productDocument.getId())
                .setName(productDocument.getName())
                .setDescription(productDocument.getDescription())
                .setPrice(productDocument.getPrice())
                .setCategory(productDocument.getCategory())
                .setPersonId(productDocument.getPersonId())
                .setTags(productDocument.getTags())
                .setHiddenLabels(productDocument.getHiddenLabels())
                .setAttributes(productDocument.getAttributes());
    }
}
