package tech.github.storecore.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.github.storecore.security.AuthenticatedUser;
import tech.github.storecore.security.CurrentUser;
import tech.github.storecore.dto.request.PersonRequest;
import tech.github.storecore.dto.response.PersonResponse;
import tech.github.storecore.service.PersonService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/persons")
public class PersonController {

    private final PersonService service;

    @PostMapping("/signup")
    public ResponseEntity<Void> create(@Valid @RequestBody PersonRequest personRequest) {
        service.create(personRequest);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<PersonResponse> me(@CurrentUser AuthenticatedUser user) {
        return ResponseEntity.ok(service.findById(user.userId()));
    }

    @PutMapping("/me")
    public ResponseEntity<PersonResponse> update(@CurrentUser AuthenticatedUser user,
            @Valid @RequestBody PersonRequest personRequest) {
        return ResponseEntity.ok(service.update(user.userId(), personRequest));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> delete(@CurrentUser AuthenticatedUser user) {
        log.debug("deleting person with id: {}", user.userId());
        service.delete(user.userId());
        return ResponseEntity.noContent().build();
    }
}