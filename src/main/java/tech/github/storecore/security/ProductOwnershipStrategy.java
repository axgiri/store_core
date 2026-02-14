package tech.github.storecore.security;

import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import tech.github.storecore.repository.ProductRepository;

@Component
@RequiredArgsConstructor
public class ProductOwnershipStrategy implements OwnershipStrategy {

    private final ProductRepository productRepository;

    @Override
    public boolean isOwner(UUID userId, Long resourceId) {
        return productRepository.existsByIdAndPersonId(resourceId, userId);
    }

    @Override
    public ResourceType supports() {
        return ResourceType.PRODUCT;
    }
}
