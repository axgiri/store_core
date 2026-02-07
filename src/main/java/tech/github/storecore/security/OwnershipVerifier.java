package tech.github.storecore.security;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import tech.github.storecore.entity.Product;
import tech.github.storecore.exception.ForbiddenException;
import tech.github.storecore.repository.ProductRepository;
import tech.github.storecore.exception.ProductNotFoundException;

@Component
@RequiredArgsConstructor
public class OwnershipVerifier {

    private final ProductRepository productRepository;

    public void verifyProductOwner(AuthenticatedUser user, Product product) {
        if (user.isPrivileged()) {
            return;
        }
        if (!product.getPerson().getId().equals(user.userId())) {
            throw new ForbiddenException("you are not the owner of this product");
        }
    }

    public Product verifyAndLoadProduct(AuthenticatedUser user, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("product not found"));
        verifyProductOwner(user, product);
        return product;
    }
}
