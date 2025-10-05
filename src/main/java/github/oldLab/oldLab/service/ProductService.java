package github.oldLab.oldLab.service;

import java.util.List;

import github.oldLab.oldLab.Enum.CategoryEnum;
import github.oldLab.oldLab.dto.request.ProductRequest;
import github.oldLab.oldLab.dto.response.ProductResponse;

public interface ProductService {
    ProductResponse create(ProductRequest request, String bearerToken);
    ProductResponse getById(Long id);
    List<ProductResponse> list(int page, int size);
    List<ProductResponse> listByPersonId(Long personId, int page, int size);
    ProductResponse update(Long id, ProductRequest request);
    void delete(Long id);

    // Elasticsearch search
    List<ProductResponse> search(String query, int page, int size);
    List<ProductResponse> searchByPerson(Long personId, String query, int page, int size);

    List<ProductResponse> listByCategory(CategoryEnum categoryEnum, int page, int size);

    List<ProductResponse> searchByCategory(CategoryEnum categoryEnum, String query, int page, int size);
}
