package github.oldLab.oldLab.search;

import github.oldLab.oldLab.entity.Product;

public final class ProductDocumentRequest {
    private ProductDocumentRequest() {}

    public static ProductDocument fromEntity(Product product) {
        ProductDocument productDocument = new ProductDocument();
        productDocument.setId(product.getId());
        productDocument.setName(product.getName());
        productDocument.setDescription(product.getDescription());
        productDocument.setPrice(product.getPrice());
        productDocument.setCategory(product.getCategory());
        productDocument.setShopId(product.getShop() != null ? product.getShop().getId() : null);
        productDocument.setTags(product.getTags());
        productDocument.setHiddenLabels(product.getHiddenLabels());
        productDocument.setAttributes(product.getAttributes());
        return productDocument;
    }
}
