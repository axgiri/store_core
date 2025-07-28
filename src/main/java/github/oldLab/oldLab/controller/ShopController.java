package github.oldLab.oldLab.controller;

import github.oldLab.oldLab.dto.request.ShopRequest;
import github.oldLab.oldLab.dto.response.ShopResponse;
import github.oldLab.oldLab.Enum.CategoryEnum;
import github.oldLab.oldLab.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shops")
public class ShopController {

    private final ShopService shopService;

    @PostMapping("/async/create")
    public ResponseEntity<Void> createShop(@RequestBody ShopRequest shopRequest) {
        log.debug("Received request to create shop: {}", shopRequest);
        shopService.createShopAsync(shopRequest);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShopResponse> getShop(@PathVariable Long id) {
        ShopResponse shopResponse = shopService.getShop(id);
        return ResponseEntity.ok(shopResponse);
    }

    @GetMapping
    public ResponseEntity<List<ShopResponse>> getAllShops(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<ShopResponse> shops = shopService.getAllShopsPaginated(page, size);
        return ResponseEntity.ok(shops);
    }

    @PutMapping("/async/{id}")
    public ResponseEntity<Void> updateShop(@PathVariable Long id, @RequestBody ShopRequest shopRequest) {
        log.debug("Received request to update shop {}: {}", id, shopRequest);
        shopService.updateShopAsync(id, shopRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShop(@PathVariable Long id) {
        shopService.deleteShop(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/category")
    public ResponseEntity<List<ShopResponse>> getShopsByCategory(@RequestParam("category") List<CategoryEnum> category) {
        List<ShopResponse> shops = shopService.getShopsByCategory(category);
        return ResponseEntity.ok(shops);
    }
}
