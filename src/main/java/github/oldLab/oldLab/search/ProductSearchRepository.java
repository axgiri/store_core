package github.oldLab.oldLab.search;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Long> {
    Page<ProductDocument> findByNameContainingOrDescriptionContaining(String name, String description, Pageable pageable);

    Page<ProductDocument> findByShopIdAndNameContainingOrShopIdAndDescriptionContaining(Long shopId1, String name, Long shopId2, String description, Pageable pageable);

    default List<ProductDocument> searchByText(String text, Pageable pageable) {
        return findByNameContainingOrDescriptionContaining(text, text, pageable).getContent();
    }

    default List<ProductDocument> searchByShopAndText(Long shopId, String text, Pageable pageable) {
        return findByShopIdAndNameContainingOrShopIdAndDescriptionContaining(shopId, text, shopId, text, pageable).getContent();
    }
}
