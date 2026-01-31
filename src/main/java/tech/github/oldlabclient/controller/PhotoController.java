package tech.github.oldlabclient.controller;

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
import tech.github.oldlabclient.dto.response.ProductPhotoResponse;
import tech.github.oldlabclient.service.PhotoService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/photos")
@RequiredArgsConstructor
@Slf4j
public class PhotoController {

    private final PhotoService service;

    @PutMapping(path = "/persons/{personId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //@PreAuthorize("@accessControlService.isSelf(authentication, #personId) or @accessControlService.isAdmin(authentication)")
    public ResponseEntity<Void> uploadPersonPhoto(@PathVariable UUID personId,
            @RequestPart("file") MultipartFile file,
            HttpServletRequest httpRequest) throws IOException {
        service.uploadForPerson(personId, file);
        return ResponseEntity.ok().build();
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
    public ResponseEntity<List<ProductPhotoResponse>> getProductPhoto(@PathVariable Long productId) {
        List<ProductPhotoResponse> response = service.loadForProduct(productId);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(response);
    }

    @PostMapping(path = "/products/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //@PreAuthorize("@accessControlService.isProductOwnerByProduct(authentication, #productId) or @accessControlService.isAdmin(authentication)")
    public ResponseEntity<Void> uploadProductPhotos(@PathVariable Long productId,
            @RequestPart("file") MultipartFile file) throws Exception {
        log.debug("upload product photos id: {}", productId);
        service.uploadForProduct(productId, file);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/products/{productId}/{objectKey}")
    //@PreAuthorize("@accessControlService.isProductOwnerByProduct(authentication, #productId) or @accessControlService.isAdmin(authentication)")
    public ResponseEntity<Void> deleteProductPhoto(@PathVariable Long productId,
            @PathVariable String objectKey) throws Exception {
        log.debug("delete product photo product id: {}, object key: {}", productId, objectKey);
        service.deleteForProduct(productId, objectKey);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/persons/{personId}")
    //@PreAuthorize("@accessControlService.isSelf(authentication, #personId) or @accessControlService.isAdmin(authentication)")
    public ResponseEntity<Void> deletePersonPhoto(@PathVariable UUID personId) throws Exception {
        service.deleteForPerson(personId);
        return ResponseEntity.noContent().build();
    }
}
// TODO: make it "friendly" and get personId from header