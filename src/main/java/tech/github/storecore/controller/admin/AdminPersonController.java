package tech.github.storecore.controller.admin;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.github.storecore.security.AuthenticatedUser;
import tech.github.storecore.security.CurrentUser;
import tech.github.storecore.security.RequireRole;
import tech.github.storecore.security.UserRole;
import tech.github.storecore.dto.request.PersonRequest;
import tech.github.storecore.dto.response.PersonResponse;
import tech.github.storecore.service.PersonService;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/persons")
@RequiredArgsConstructor
@RequireRole({UserRole.ADMIN, UserRole.MODERATOR})
public class AdminPersonController {

    private final PersonService service;

    @PutMapping("/{id}")
    public ResponseEntity<PersonResponse> update(@CurrentUser AuthenticatedUser user,
            @PathVariable UUID id,
            @Valid @RequestBody PersonRequest personRequest) {
        log.debug("admin {} updating person {}", user.userId(), id);
        return ResponseEntity.ok(service.update(id, personRequest));
    }

    @DeleteMapping("/{id}")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<Void> delete(@CurrentUser AuthenticatedUser user,
            @PathVariable UUID id) {
        log.debug("admin {} deleting person {}", user.userId(), id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
