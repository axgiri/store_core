package github.oldLab.oldLab.service;

import java.util.List;

import github.oldLab.oldLab.dto.request.ProductRequest;
import github.oldLab.oldLab.dto.response.ProductResponse;

public interface ProductService {
    ProductResponse create(ProductRequest request, String bearerToken);
    ProductResponse getById(Long id);
    List<ProductResponse> list(int page, int size);
    List<ProductResponse> listByShop(Long shopId, int page, int size);
    ProductResponse update(Long id, ProductRequest request);
    void delete(Long id);

    // Elasticsearch search
    List<ProductResponse> search(String query, int page, int size);
    List<ProductResponse> searchByShop(Long shopId, String query, int page, int size);
}
