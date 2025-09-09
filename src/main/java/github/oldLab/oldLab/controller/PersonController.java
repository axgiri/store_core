package github.oldLab.oldLab.controller;

import java.util.List;

import github.oldLab.oldLab.dto.request.ResetPasswordRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import github.oldLab.oldLab.dto.request.LoginRequest;
import github.oldLab.oldLab.dto.request.RefreshRequest;
import github.oldLab.oldLab.dto.request.PersonRequest;
import github.oldLab.oldLab.dto.response.AuthResponse;
import github.oldLab.oldLab.dto.response.PersonResponse;
import github.oldLab.oldLab.serviceImpl.PersonServiceImpl;
import github.oldLab.oldLab.serviceImpl.RateLimiterServiceImpl;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/persons")
public class PersonController {

    private final PersonServiceImpl service;
    private final RateLimiterServiceImpl rateLimiterService;

    @PostMapping("/async/signup")
    public ResponseEntity<Void> createAsync(@Valid @RequestBody PersonRequest personRequest, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("creating person: {}", personRequest);
            service.createAsync(personRequest);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest httpRequest){
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            AuthResponse authResponse = service.authenticate(loginRequest);
            return ResponseEntity.ok(authResponse);
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request, HttpServletRequest httpRequest){
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            AuthResponse response = service.refreshAccessToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @PostMapping("/revoke")
    public ResponseEntity<Void> revoke(@Valid @RequestBody RefreshRequest request, HttpServletRequest httpRequest){
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            service.revoke(request.getRefreshToken());
            return ResponseEntity.ok().build();
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @PostMapping("/revokeAll")
    public ResponseEntity<Void> revokeAll(@Valid @RequestBody RefreshRequest request, HttpServletRequest httpRequest){
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            service.revokeAll(request.getRefreshToken());
            return ResponseEntity.ok().build();
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @GetMapping("/findById/{id}")
    @PreAuthorize("@accessControlService.isSelf(authentication, #id) or @accessControlService.isModerator(authentication) or @accessControlService.isAdmin(authentication)")
    public ResponseEntity<PersonResponse> findById(@PathVariable Long id, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        
        if (bucket.tryConsume(1)) {
            log.debug("finding person with id: {}", id);
            return ResponseEntity.ok(service.findById(id));
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @GetMapping("/findByPhoneNumber/{phoneNumber}")
    @PreAuthorize("@accessControlService.isSelfByPhoneNumber(authentication, #phoneNumber) or @accessControlService.isModerator(authentication) or @accessControlService.isAdmin(authentication)")
    public ResponseEntity<PersonResponse> findByPhoneNumber(@PathVariable String phoneNumber, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        
        if (bucket.tryConsume(1)) {
            log.debug("finding person with phone number: {}", phoneNumber);
            return ResponseEntity.ok(service.findByPhoneNumber(phoneNumber));
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("@accessControlService.isSelf(authentication, #id) or @accessControlService.isAdmin(authentication)")
    public ResponseEntity<PersonResponse> update(@PathVariable Long id,@Valid @RequestBody PersonRequest personRequest, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("updating person with id: {}", id);
            return ResponseEntity.ok(service.update(id, personRequest));
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("@accessControlService.isSelf(authentication, #id) or @accessControlService.isAdmin(authentication)")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("deleting person with id: {}", id);
            service.delete(id);
            return ResponseEntity.ok().build();
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validate(@RequestHeader("Authorization") String token, HttpServletRequest httpRequest){
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            service.validateToken(token);
            return ResponseEntity.ok("validation successful");
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @GetMapping("/getMyColleagues")
    @PreAuthorize("@accessControlService.hasCompany(authentication)")
    public ResponseEntity<List<PersonResponse>> getColleagues(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("getting colleagues for token: {}", token);
            service.validateToken(token);
            List<PersonResponse> colleagues = service.getColleaguesAsync(token, page, size);
            return ResponseEntity.ok(colleagues);
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @PutMapping("/updatePassword")
    @PreAuthorize("@accessControlService.isSelfByPhoneNumber(authentication, #loginRequest.phoneNumber)")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody LoginRequest loginRequest,@RequestParam String oldPassword, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("updating password for phone number: {}", loginRequest.getPhoneNumber());
            try {
                service.updatePasswordAsync(loginRequest, oldPassword).join();
            } catch (java.util.concurrent.CompletionException e) {
                if (e.getCause() instanceof RuntimeException re) {
                    throw re;
                }
                throw e;
            }
            return ResponseEntity.ok().build();
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @PostMapping("/requestPasswordReset")
    public ResponseEntity<Void> requestPasswordReset(
            @Valid @RequestBody String contact, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("waiting request for reset password from contact: {}", contact);
            service.requestPasswordReset(contact);
            return ResponseEntity.accepted().build();
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("updating password for contact: {}", request.getContact());
            service.resetPassword(request);
            return ResponseEntity.ok().build();
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

}