package github.oldLab.oldLab.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import github.oldLab.oldLab.dto.request.ProductRequest;
import github.oldLab.oldLab.dto.response.ProductResponse;
import github.oldLab.oldLab.service.ProductService;
import github.oldLab.oldLab.serviceImpl.RateLimiterServiceImpl;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
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
            return ResponseEntity.ok(productService.get(id));
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

    @PutMapping("/{id}")
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
}
