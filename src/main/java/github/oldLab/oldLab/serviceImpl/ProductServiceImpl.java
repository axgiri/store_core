package github.oldLab.oldLab.serviceImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import github.oldLab.oldLab.dto.request.ProductRequest;
import github.oldLab.oldLab.dto.response.ProductResponse;
import github.oldLab.oldLab.entity.Product;
import github.oldLab.oldLab.entity.Shop;
import github.oldLab.oldLab.exception.ProductNotFoundException;
import github.oldLab.oldLab.exception.ShopNotFoundException;
import github.oldLab.oldLab.repository.ProductRepository;
import github.oldLab.oldLab.service.ProductService;
import github.oldLab.oldLab.search.ProductSearchRepository;
import github.oldLab.oldLab.search.ProductDocumentRequest;
import github.oldLab.oldLab.search.ProductDocumentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final ShopServiceImpl shopService;
    private final TokenServiceImpl tokenService;
    private final PersonServiceImpl personService;

    private final ProductSearchRepository productSearchRepository;

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request, String bearerToken) {
        String token = bearerToken.startsWith("Bearer ") ? bearerToken.substring(7) : bearerToken;
        String phone = tokenService.extractUsername(token);
        Long personId = personService.getIdFromPhoneNumber(phone);
        Long companyId = personService.getCompanyIdByPersonId(personId);
        if (companyId == null) {
            throw new ShopNotFoundException("User has no companyId; create a shop first");
        }
        Shop shopRef = shopService.getReferenceById(companyId);
        Product saved = repository.save(request.toEntity(shopRef));
        productSearchRepository.save(ProductDocumentRequest.fromEntity(saved));
        return ProductResponse.fromEntityToDto(saved);
    }

    @Override
    public ProductResponse getById(Long id) {
        Product p = repository.findById(id).orElseThrow(() -> new ProductNotFoundException("Product not found"));
        return ProductResponse.fromEntityToDto(p);
    }

    @Override
    public List<ProductResponse> list(int page, int size) {
        return repository.findAll(PageRequest.of(page, size)).getContent().stream()
                .map(ProductResponse::fromEntityToDto)
                .toList();
    }

    @Override
    public List<ProductResponse> listByShop(Long shopId, int page, int size) {
        if (!shopService.existsById(shopId)) {
            throw new ShopNotFoundException("Shop not found: " + shopId);
        }
        return repository.findByShopId(shopId, PageRequest.of(page, size))
                .map(ProductResponse::fromEntityToDto)
                .getContent();
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product existing = repository.findById(id).orElseThrow(() -> new ProductNotFoundException("Product not found"));
        BeanUtils.copyProperties(request, existing, "id", "version", "shop");
        Product saved = repository.save(existing);
    productSearchRepository.save(ProductDocumentRequest.fromEntity(saved));
        return ProductResponse.fromEntityToDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
        productSearchRepository.deleteById(id);
    }

    @Override
    public List<ProductResponse> search(String query, int page, int size) {
    return productSearchRepository.searchByText(query, PageRequest.of(page, size))
        .stream().map(ProductDocumentResponse::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> searchByShop(Long shopId, String query, int page, int size) {
        
        if (!shopService.existsById(shopId)) {
            throw new ShopNotFoundException("Shop not found: " + shopId);
        }

        return productSearchRepository.searchByShopAndText(shopId, query, PageRequest.of(page, size))
                .stream().map(ProductDocumentResponse::toResponse).collect(Collectors.toList());
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
}
