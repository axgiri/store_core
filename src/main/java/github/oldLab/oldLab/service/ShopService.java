package github.oldLab.oldLab.service;

import github.oldLab.oldLab.Enum.CategoryEnum;
import github.oldLab.oldLab.dto.request.ShopRequest;
import github.oldLab.oldLab.dto.response.ShopResponse;
import java.util.List;

public interface ShopService {
    void createShopAsync(ShopRequest shopRequest);
    
    ShopResponse getShop(Long id);

    List<ShopResponse> getAllShopsPaginated(int page, int size);

    void updateShopAsync(Long id, ShopRequest shop);

    void deleteShop(Long id);

    List<ShopResponse> getShopsByCategory(List<CategoryEnum> category);
}
