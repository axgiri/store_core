package github.oldLab.oldLab.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import github.oldLab.oldLab.dto.request.ActivateRequest;
import github.oldLab.oldLab.dto.response.AuthResponse;
import github.oldLab.oldLab.serviceImpl.ActivateServiceImpl;
import github.oldLab.oldLab.serviceImpl.RateLimiterServiceImpl;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/activate")
public class ActivateController {
    
    private final ActivateServiceImpl service;
    private final RateLimiterServiceImpl rateLimiterService;

    @PostMapping("/activate")
    public ResponseEntity<Void> activate(@Valid @RequestBody ActivateRequest request, HttpServletRequest httpRequest){
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        
        if (bucket.tryConsume(1)) {
            log.debug("activating account with email: {}", request.getEmail());
            service.setActive(request);
            return ResponseEntity.ok().build();
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @PostMapping("/send/activate/{phoneNumber}")
    public ResponseEntity<Void> sendOtp(@PathVariable String phoneNumber, HttpServletRequest httpRequest){
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("sending OTP to phone number: {}", phoneNumber);
            service.sendOtp(phoneNumber);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @PostMapping("/resend/activate/{phoneNumber}")
    public ResponseEntity<Void> resendOtp(@PathVariable String phoneNumber, HttpServletRequest httpRequest){
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("resending OTP to phone number: {}", phoneNumber);
            service.resendOtp(phoneNumber);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody ActivateRequest request, HttpServletRequest httpRequest){
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        
        if (bucket.tryConsume(1)) {
            log.debug("logging in user with phone number: {}", request.getEmail());
            AuthResponse response = service.login(request.getEmail(), request.getOtp());
            return ResponseEntity.ok(response);
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @PostMapping("/send/login/{phoneNumber}")
    public ResponseEntity<Void> sendLoginOtp(@PathVariable String phoneNumber, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("sending login OTP to phone number: {}", phoneNumber);
            service.sendLoginOtp(phoneNumber);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }
}