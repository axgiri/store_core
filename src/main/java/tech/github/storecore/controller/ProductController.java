package tech.github.storecore.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.github.storecore.security.AuthenticatedUser;
import tech.github.storecore.security.CurrentUser;
import tech.github.storecore.security.OwnershipVerifier;
import tech.github.storecore.dto.request.ProductRequest;
import tech.github.storecore.dto.response.ProductResponse;
import tech.github.storecore.enumeration.CategoryEnum;
import tech.github.storecore.service.ProductService;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final OwnershipVerifier ownershipVerifier;

    @PostMapping
    public ResponseEntity<ProductResponse> create(@CurrentUser AuthenticatedUser user,
            @RequestBody @Validated ProductRequest request) {
        return ResponseEntity.ok(productService.create(request, user.userId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@CurrentUser AuthenticatedUser user,
            @PathVariable Long id,
            @RequestBody ProductRequest request) {
        ownershipVerifier.verifyAndLoadProduct(user, id);
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@CurrentUser AuthenticatedUser user,
            @PathVariable Long id) {
        ownershipVerifier.verifyAndLoadProduct(user, id);
        productService.delete(id);
        return ResponseEntity.noContent().build();
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