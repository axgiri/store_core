package github.oldLab.oldLab.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import github.oldLab.oldLab.Enum.CategoryEnum;
import github.oldLab.oldLab.dto.request.ProductRequest;
import github.oldLab.oldLab.dto.response.ProductResponse;
import github.oldLab.oldLab.service.ProductService;
import github.oldLab.oldLab.serviceImpl.RateLimiterServiceImpl;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final RateLimiterServiceImpl rateLimiterService;

    @PostMapping
    public ResponseEntity<ProductResponse> create(@RequestBody @Validated ProductRequest request,
                                                  @RequestHeader("Authorization") String header,
                                                  HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("creating product: {}", request);
            return ResponseEntity.ok(productService.create(request, header));
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> get(@PathVariable Long id, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("getting product with id: {}", id);
            return ResponseEntity.ok(productService.getById(id));
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<ProductResponse>> list(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size,
                                                      HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("listing products page: {}, size: {}", page, size);
            return ResponseEntity.ok(productService.list(page, size));
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @GetMapping("/persons/{personId}")
    public ResponseEntity<List<ProductResponse>> listByPersonId(@PathVariable Long personId,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "20") int size,
                                                            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("listing products by personId: {}, page: {}, size: {}", personId, page, size);
            return ResponseEntity.ok(productService.listByPersonId(personId, page, size));
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("@accessControlService.isProductOwnerByProduct(authentication, #id) or @accessControlService.isAdmin(authentication)")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id,
                                                  @RequestBody ProductRequest request,
                                                  HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("updating product id: {} with: {}", id, request);
            return ResponseEntity.ok(productService.update(id, request));
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@accessControlService.isProductOwnerByProduct(authentication, #id) or @accessControlService.isAdmin(authentication)")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("deleting product id: {}", id);
            productService.delete(id);
            return ResponseEntity.ok().build();
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> search(@RequestParam("q") String query,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size,
                                                        HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("searching products q: {}, page: {}, size: {}", query, page, size);
            return ResponseEntity.ok(productService.search(query, page, size));
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @GetMapping("/persons/{personId}/search")
    public ResponseEntity<List<ProductResponse>> searchByPerson(@PathVariable Long personId,
                                                              @RequestParam("q") String query,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size,
                                                              HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("searching products by personId: {}, q: {}, page: {}, size: {}", personId, query, page, size);
            return ResponseEntity.ok(productService.searchByPerson(personId, query, page, size));
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @GetMapping("/categories/{categoryEnum}")
    public ResponseEntity<List<ProductResponse>> listByCategory(@PathVariable CategoryEnum categoryEnum,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "20") int size,
                                                                 HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("listing products by category: {}, page: {}, size: {}", categoryEnum, page, size);
            return ResponseEntity.ok(productService.listByCategory(categoryEnum, page, size));
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @GetMapping("/search/categories/{categoryEnum}")
    public ResponseEntity<List<ProductResponse>> searchByCategory(@PathVariable CategoryEnum categoryEnum,
                                                                  @RequestParam("q") String query,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "20") int size,
                                                                  HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("searching products by category: {}, q: {}, page: {}, size: {}", categoryEnum, query, page, size);
            return ResponseEntity.ok(productService.searchByCategory(categoryEnum, query, page, size));
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }
}