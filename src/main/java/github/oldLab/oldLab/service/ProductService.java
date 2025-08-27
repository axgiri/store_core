package github.oldLab.oldLab.service;

import java.util.List;

import github.oldLab.oldLab.dto.request.ProductRequest;
import github.oldLab.oldLab.dto.response.ProductResponse;

public interface ProductService {
    ProductResponse create(ProductRequest request, String bearerToken);
    ProductResponse get(Long id);
    List<ProductResponse> list(int page, int size);
    ProductResponse update(Long id, ProductRequest request);
    void delete(Long id);

    // Elasticsearch search
    List<ProductResponse> search(String query, int page, int size);
}
