package github.oldLab.oldLab.search;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Long> {
    // derive a simple multi-field query using custom implementation
    Page<ProductDocument> findByNameContainingOrDescriptionContaining(String name, String description, Pageable pageable);

    default List<ProductDocument> searchByText(String text, Pageable pageable) {
        return findByNameContainingOrDescriptionContaining(text, text, pageable).getContent();
    }
}
