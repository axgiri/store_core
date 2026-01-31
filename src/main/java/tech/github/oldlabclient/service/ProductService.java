package tech.github.oldlabclient.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.github.oldlabclient.Enum.CategoryEnum;
import tech.github.oldlabclient.dto.request.ProductRequest;
import tech.github.oldlabclient.dto.response.ProductResponse;
import tech.github.oldlabclient.entity.Person;
import tech.github.oldlabclient.entity.Product;
import tech.github.oldlabclient.exception.ProductNotFoundException;
import tech.github.oldlabclient.exception.UserNotFoundException;
import tech.github.oldlabclient.repository.ProductRepository;
import tech.github.oldlabclient.search.ProductDocumentRequest;
import tech.github.oldlabclient.search.ProductDocumentResponse;
import tech.github.oldlabclient.search.ProductSearchRepository;
import tech.github.oldlabclient.service.ProductService;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService{

    private final ProductRepository repository;
    private final PersonService personService;

    private final ProductSearchRepository productSearchRepository;

    @Transactional
     //TODO: no zero trust now as i mentioned before in SecurityConfiguration 117 line
    // because of that we will get token from header (gateway) . zero trust will be implemented later
    public ProductResponse create(ProductRequest request, UUID personId) {
        Person personReference = personService.getReferenceById(personId);
        Product saved = repository.save(request.toEntity(personReference));
        productSearchRepository.save(ProductDocumentRequest.fromEntity(saved));
        return ProductResponse.fromEntity(saved);
    }

    public ProductResponse getById(Long id) {
        Product p = repository.findById(id).orElseThrow(() -> new ProductNotFoundException("Product not found"));
        return ProductResponse.fromEntity(p);
    }

    public List<ProductResponse> list(int page, int size) {
        return repository.findAll(PageRequest.of(page, size)).getContent().stream()
                .map(ProductResponse::fromEntity)
                .toList();
    }

    public List<ProductResponse> listByPersonId(UUID personId, int page, int size) {
        if (!personService.existsById(personId)) {
            throw new UserNotFoundException("Person not found: " + personId);
        }
        return repository.findByPersonId(personId, PageRequest.of(page, size))
                .map(ProductResponse::fromEntity)
                .getContent();
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product existing = repository.findById(id).orElseThrow(() -> new ProductNotFoundException("Product not found"));
        BeanUtils.copyProperties(request, existing, "id", "version");
        Product saved = repository.save(existing);
        productSearchRepository.save(ProductDocumentRequest.fromEntity(saved));
        return ProductResponse.fromEntity(saved);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
        productSearchRepository.deleteById(id);
    }

    public List<ProductResponse> search(String query, int page, int size) {
        return productSearchRepository.search(query, PageRequest.of(page, size))
                .stream().map(ProductDocumentResponse::toResponse).toList();
    }
    public List<ProductResponse> searchByPerson(UUID personId, String query, int page, int size) {

        if (!personService.existsById(personId)) {
            throw new UserNotFoundException("User not found: " + personId);
        }

    return productSearchRepository.searchByPerson(personId, query, PageRequest.of(page, size))
        .stream().map(ProductDocumentResponse::toResponse).toList();
    }

    public Product findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("product not found with id: " + id));
    }

    public Product getReferenceByIdIfExists(Long id) {
        if (!repository.existsById(id)) {
            throw new ProductNotFoundException("product not found with id: " + id);
        }
        return repository.getReferenceById(id);
    }

    public List<ProductResponse> listByCategory(CategoryEnum categoryEnum, int page, int size) {
        return repository.findByCategory(categoryEnum, PageRequest.of(page, size))
                .map(ProductResponse::fromEntity)
                .getContent();
    }

    public List<ProductResponse> searchByCategory(CategoryEnum categoryEnum, String query, int page, int size) {
        return productSearchRepository.searchByCategory(categoryEnum, query, PageRequest.of(page, size))
                .stream().map(ProductDocumentResponse::toResponse).toList();
    }
}
