package github.oldLab.oldLab.controller;

import github.oldLab.oldLab.Enum.CategoryEnum;
import github.oldLab.oldLab.dto.request.ShopRequest;
import github.oldLab.oldLab.dto.response.ShopResponse;
import github.oldLab.oldLab.service.ShopService;
import github.oldLab.oldLab.serviceImpl.RateLimiterServiceImpl;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
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
    private final RateLimiterServiceImpl rateLimiterService;

    @PostMapping("/async/create")
    public ResponseEntity<Void> createShop(@RequestBody ShopRequest shopRequest,
                                           @RequestHeader("Authorization") String header,
                                           HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("Received request to create shop: {}", shopRequest);
            String token = header.substring(7);
            shopService.createShopAsync(shopRequest, token);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShopResponse> getShop(@PathVariable Long id, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            ShopResponse shopResponse = shopService.getShop(id);
            return ResponseEntity.ok(shopResponse);
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ShopResponse>> getAllShops(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            List<ShopResponse> shops = shopService.getAllShopsPaginated(page, size);
            return ResponseEntity.ok(shops);
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @PutMapping("/async/{id}")
    public ResponseEntity<Void> updateShop(@PathVariable Long id,
                                           @RequestBody ShopRequest shopRequest,
                                           HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("Received request to update shop {}: {}", id, shopRequest);
            shopService.updateShopAsync(id, shopRequest);
            return ResponseEntity.ok().build();
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShop(@PathVariable Long id, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            shopService.deleteShop(id);
            return ResponseEntity.ok().build();
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @GetMapping("/category")
    public ResponseEntity<List<ShopResponse>> getShopsByCategory(
            @RequestParam("category") List<CategoryEnum> category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            List<ShopResponse> shops = shopService.getShopsByCategory(category, page, size);
            return ResponseEntity.ok(shops);
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }
}
