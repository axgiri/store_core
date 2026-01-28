package tech.github.oldlabclient.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import tech.github.oldlabclient.dto.request.PersonRequest;
import tech.github.oldlabclient.dto.response.PersonResponse;
import tech.github.oldlabclient.service.PersonService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/persons")
public class PersonController {

    private final PersonService service;

    @PostMapping("/async/signup")
    public ResponseEntity<Void> createAsync(@Valid @RequestBody PersonRequest personRequest) {
        service.createAsync(personRequest);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @GetMapping("/findById/{id}")
    public ResponseEntity<PersonResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("@accessControlService.isSelf(authentication, #id) or @accessControlService.isAdmin(authentication)")
    public ResponseEntity<PersonResponse> update(@PathVariable UUID id, @Valid @RequestBody PersonRequest personRequest) {
        return ResponseEntity.ok(service.update(id, personRequest));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("@accessControlService.isSelf(authentication, #id) or @accessControlService.isAdmin(authentication)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        log.debug("deleting person with id: {}", id);
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}

// TODO: exists by id
// is he have already report or review for this entity