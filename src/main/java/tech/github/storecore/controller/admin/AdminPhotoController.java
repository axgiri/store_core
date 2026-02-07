package tech.github.storecore.controller.admin;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.github.storecore.security.AuthenticatedUser;
import tech.github.storecore.security.CurrentUser;
import tech.github.storecore.security.RequireRole;
import tech.github.storecore.security.UserRole;
import tech.github.storecore.service.PhotoService;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/photos")
@RequiredArgsConstructor
@RequireRole({UserRole.ADMIN, UserRole.MODERATOR})
public class AdminPhotoController {

    private final PhotoService service;

    @PutMapping(path = "/persons/{personId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadPersonPhoto(@CurrentUser AuthenticatedUser user,
            @PathVariable UUID personId,
            @RequestPart("file") MultipartFile file) throws IOException {
        log.debug("admin {} uploading photo for person {}", user.userId(), personId);
        service.uploadForPerson(personId, file);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/persons/{personId}")
    public ResponseEntity<Void> deletePersonPhoto(@CurrentUser AuthenticatedUser user,
            @PathVariable UUID personId) {
        log.debug("admin {} deleting photo for person {}", user.userId(), personId);
        service.deleteForPerson(personId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/products/{productId}/{objectKey}")
    public ResponseEntity<Void> deleteProductPhoto(@CurrentUser AuthenticatedUser user,
            @PathVariable Long productId,
            @PathVariable String objectKey) {
        log.debug("admin {} deleting product photo: productId={}, key={}", user.userId(), productId, objectKey);
        service.deleteForProduct(productId, objectKey);
        return ResponseEntity.noContent().build();
    }
}
