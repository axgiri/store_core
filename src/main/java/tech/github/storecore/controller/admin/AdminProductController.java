package tech.github.storecore.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.github.storecore.security.AuthenticatedUser;
import tech.github.storecore.security.CurrentUser;
import tech.github.storecore.security.RequireRole;
import tech.github.storecore.security.UserRole;
import tech.github.storecore.service.ProductService;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@RequireRole({UserRole.ADMIN, UserRole.MODERATOR})
public class AdminProductController {

    private final ProductService productService;

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@CurrentUser AuthenticatedUser user, @PathVariable Long id) {
        log.debug("admin {} deleting product {}", user.userId(), id);
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
