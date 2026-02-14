package tech.github.storecore.controller;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.github.storecore.dto.response.ProductPhotoResponse;
import tech.github.storecore.security.AuthenticatedUser;
import tech.github.storecore.security.CurrentUser;
import tech.github.storecore.security.ResourceType;
import tech.github.storecore.security.VerifyOwnership;
import tech.github.storecore.service.PhotoService;

@RestController
@RequestMapping("/api/v1/photos")
@RequiredArgsConstructor
@Slf4j
public class PhotoController {

    private final PhotoService service;

    @PutMapping(path = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadPersonPhoto(@CurrentUser AuthenticatedUser user,
            @RequestPart("file") MultipartFile file) throws IOException {
        service.uploadForPerson(user.userId(), file);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<byte[]> getOwnPhoto(@CurrentUser AuthenticatedUser user) {
        byte[] bytes = service.loadForPerson(user.userId());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("image/webp"))
                .cacheControl(CacheControl.noCache())
                .body(bytes);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deletePersonPhoto(@CurrentUser AuthenticatedUser user) {
        service.deleteForPerson(user.userId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/persons/{personId}")
    public ResponseEntity<byte[]> getPersonPhoto(@PathVariable UUID personId) {
        byte[] bytes = service.loadForPerson(personId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("image/webp"))
                .cacheControl(CacheControl.noCache())
                .body(bytes);
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<List<ProductPhotoResponse>> getProductPhotos(@PathVariable Long productId) {
        List<ProductPhotoResponse> response = service.loadForProduct(productId);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(response);
    }

    @VerifyOwnership(value = ResourceType.PRODUCT, idParam = "productId")
    @PostMapping(path = "/products/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadProductPhoto(@CurrentUser AuthenticatedUser user,
            @PathVariable Long productId,
            @RequestPart("file") MultipartFile file) throws IOException {
        log.debug("upload product photo id: {}, user: {}", productId, user.userId());
        service.uploadForProduct(productId, file);
        return ResponseEntity.ok().build();
    }

    @VerifyOwnership(value = ResourceType.PRODUCT, idParam = "productId")
    @DeleteMapping("/products/{productId}/{objectKey}")
    public ResponseEntity<Void> deleteProductPhoto(@CurrentUser AuthenticatedUser user,
            @PathVariable Long productId,
            @PathVariable String objectKey) {
        log.debug("delete product photo product id: {}, key: {}, user: {}", productId, objectKey, user.userId());
        service.deleteForProduct(productId, objectKey);
        return ResponseEntity.noContent().build();
    }
}