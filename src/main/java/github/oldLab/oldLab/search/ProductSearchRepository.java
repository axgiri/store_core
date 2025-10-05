package github.oldLab.oldLab.search;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.annotations.Query;

import github.oldLab.oldLab.Enum.CategoryEnum;

public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Long> {

    @Query("{\"bool\":{\"should\":[{\"multi_match\":{\"query\":\"?0\",\"fields\":[\"name^5\",\"description^3\"],\"operator\":\"and\"}}]}}")
    Page<ProductDocument> search(String text, Pageable pageable);

    @Query("{\"bool\":{\"must\":[{\"term\":{\"personId\":{\"value\":\"?0\"}}},{\"multi_match\":{\"query\":\"?1\",\"fields\":[\"name^3\",\"description\"],\"operator\":\"and\"}}]}}")
    Page<ProductDocument> searchByPerson(Long personId, String text, Pageable pageable);

    @Query("{\"bool\":{\"must\":[{\"term\":{\"category\":{\"value\":\"?0\"}}},{\"multi_match\":{\"query\":\"?1\",\"fields\":[\"name^3\",\"description\"],\"operator\":\"and\"}}]}}")
    Page<ProductDocument> searchByCategory(CategoryEnum category, String text, Pageable pageable);
}
