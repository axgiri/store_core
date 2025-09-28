package github.oldLab.oldLab.search;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Long> {
    Page<ProductDocument> findByNameContainingOrDescriptionContaining(String name, String description, Pageable pageable);

    Page<ProductDocument> findByPersonIdAndNameContainingOrPersonIdAndDescriptionContaining(Long personId1, String name, Long personId2, String description, Pageable pageable);

    default List<ProductDocument> searchByText(String text, Pageable pageable) {
        return findByNameContainingOrDescriptionContaining(text, text, pageable).getContent();
    }

    default List<ProductDocument> searchByPersonAndText(Long personId, String text, Pageable pageable) {
        return findByPersonIdAndNameContainingOrPersonIdAndDescriptionContaining(personId, text, personId, text, pageable).getContent();
    }
}
