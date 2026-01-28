package tech.github.oldlabclient.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.github.oldlabclient.Enum.CategoryEnum;
import tech.github.oldlabclient.dto.request.ProductRequest;
import tech.github.oldlabclient.dto.response.ProductResponse;
import tech.github.oldlabclient.service.ProductService;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> create(@RequestBody @Validated ProductRequest request,
            @RequestHeader("Authorization") UUID userId) {
        return ResponseEntity.ok(productService.create(request, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @GetMapping("/list")
    public ResponseEntity<List<ProductResponse>> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.list(page, size));
    }

    @GetMapping("/persons/{personId}")
    public ResponseEntity<List<ProductResponse>> listByPersonId(@PathVariable UUID personId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(productService.listByPersonId(personId, page, size));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@accessControlService.isProductOwnerByProduct(authentication, #id) or @accessControlService.isAdmin(authentication)")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id,
            @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@accessControlService.isProductOwnerByProduct(authentication, #id) or @accessControlService.isAdmin(authentication)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> search(@RequestParam("q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.search(query, page, size));
    }

    @GetMapping("/persons/{personId}/search")
    public ResponseEntity<List<ProductResponse>> searchByPerson(@PathVariable UUID personId,
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.searchByPerson(personId, query, page, size));
    }

    @GetMapping("/list/{categoryEnum}")
    public ResponseEntity<List<ProductResponse>> listByCategory(@PathVariable CategoryEnum categoryEnum,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.listByCategory(categoryEnum, page, size));
    }

    @GetMapping("/search/categories/{categoryEnum}")
    public ResponseEntity<List<ProductResponse>> searchByCategory(@PathVariable CategoryEnum categoryEnum,
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.searchByCategory(categoryEnum, query, page, size));
    }
}