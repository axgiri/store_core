package github.oldLab.oldLab.serviceImpl;

import github.oldLab.oldLab.Enum.CategoryEnum;
import github.oldLab.oldLab.dto.request.ShopRequest;
import github.oldLab.oldLab.dto.response.ShopResponse;
import github.oldLab.oldLab.entity.Shop;
import github.oldLab.oldLab.exception.ShopNotFoundException;
import github.oldLab.oldLab.repository.ShopRepository;
import github.oldLab.oldLab.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    @Qualifier("asyncExecutor")
    private final TaskExecutor taskExecutor;

    private final ShopRepository repository;
    private final TokenServiceImpl tokenService;
    private final PersonServiceImpl personService;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Override
    public void createShopAsync(ShopRequest shopRequest, String token) {
        log.info("creating shop with name: {}", shopRequest.getName());
        taskExecutor.execute(() -> {
            
            String email = tokenService.extractUsername(token);
            Long personId = personService.getIdFromEmail(email);
            var personReference = personService.getReferenceById(personId);

            Shop shop = shopRequest.toEntity()
                .setOwnerId(personReference);

            Shop savedShop = repository.save(shop);

            personService.setCompanyIdForExistingPerson(personId, savedShop.getId());
            log.info("created shop with name: {}", shopRequest.getName());
        });
    }

    public ShopResponse getShop(Long id) {
        Shop shop = repository.findById(id)
                .orElseThrow(() -> new ShopNotFoundException("shop not found with id: " + id));
        return ShopResponse.fromEntityToDto(shop);
    }

    public Shop findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ShopNotFoundException("shop not found with id: " + id));
    }

    public List<ShopResponse> getAllShopsPaginated(int page, int size) {
        return repository.findAll(PageRequest.of(page, size)).getContent().stream()
                .map(ShopResponse::fromEntityToDto)
                .toList();
    }

    public void updateShopAsync(Long id, ShopRequest dto) {
        taskExecutor.execute(() -> {
            if (!repository.existsById(id)) {
                throw new ShopNotFoundException("shop not found with id: " + id);
            }
            Shop shop = repository.findById(id)
                .orElseThrow(() -> new ShopNotFoundException("shop not found with id: " + id));
            BeanUtils.copyProperties(dto, shop, "id", "version");
            repository.save(shop);
            log.info("updated shop with id: {}", id);
        });
    }

    public void deleteShop(Long id) {
        repository.deleteById(id);
    }

    public List<ShopResponse> getShopsByCategory(List<CategoryEnum> category, int page, int size) {
        if (category == null || category.isEmpty()) {
            throw new ShopNotFoundException("Category list is empty or null.");
        }
        log.info("fetching shops by category: {}", category);
        List<Shop> shops = repository.findByCategoryIn(category);
        
        if (shops.isEmpty()) {
            throw new ShopNotFoundException("no shops found for category: " + category);
        }

        return shops.stream()
                .map(ShopResponse::fromEntityToDto)
                .toList();
    }

    public Shop getReferenceById(Long id) {
        return repository.getReferenceById(id);
    }

    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    public Shop getReferenceByIdIfExists(Long id) {
        if (!repository.existsById(id)) {
            throw new ShopNotFoundException("shop not found with id: " + id);
        }
        return repository.getReferenceById(id);
    }
}