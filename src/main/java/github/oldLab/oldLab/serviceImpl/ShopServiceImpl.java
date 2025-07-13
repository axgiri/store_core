package github.oldLab.oldLab.serviceImpl;

import github.oldLab.oldLab.Enum.CategoryEnum;
import github.oldLab.oldLab.dto.request.ShopRequest;
import github.oldLab.oldLab.dto.response.ShopResponse;
import github.oldLab.oldLab.entity.Shop;
import github.oldLab.oldLab.repository.ShopRepository;
import github.oldLab.oldLab.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    @Qualifier("asyncExecutor")
    private final TaskExecutor taskExecutor;

    private final ShopRepository repository;

    @Override
    public void createShopAsync(ShopRequest shopRequest) {
        log.info("creating shop with name: {}", shopRequest.getName());
        taskExecutor.execute(() -> {
            repository.save(shopRequest.toEntity());
            log.info("created shop with name: {}", shopRequest.getName());
        });
    }

    public ShopResponse getShop(Long id) {
        Shop shop = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("shop not found with id: " + id)); //TODO:make new exception
        return ShopResponse.fromEntityToDto(shop);
    }

    public List<Shop> getAllShopsPaginated() {
        return repository.findAll(); //TODO: implement pagination
    }

    public void updateShopAsync(Long id, ShopRequest shop) {
        taskExecutor.execute(() -> {
            repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("shop not found with id: " + id)); //TODO:make new exception
            
            repository.save(shop.toEntity());
            log.info("updated shop with id: {}", id);
        });
    }

    public void deleteShop(Long id) {
        repository.deleteById(id);
    }

    public List<Shop> getShopsByCategory(CategoryEnum category) {
        log.info("fetching shops by category: {}", category);
        List<Shop> shops = repository.findByCategory(category);
        
        if (shops.isEmpty()) {
            throw new RuntimeException("no shops found for category: " + category); //TODO: make new exception
        }

        return shops;
    }
}