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
import github.oldLab.oldLab.service.ActivateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/activate")
public class ActivateController {
    
    public final ActivateService service;

    @PostMapping("/activate")
    public ResponseEntity<Void> activate(@Valid @RequestBody ActivateRequest request){
        log.debug("activating account with phone number: {}", request.getPhoneNumber());
        service.setActive(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send/activate/{phoneNumber}")
    public ResponseEntity<Void> sendOtp(@PathVariable String phoneNumber){
        log.debug("sending OTP to phone number: {}", phoneNumber);
        service.sendOtp(phoneNumber);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/resend/activate/{phoneNumber}")
    public ResponseEntity<Void> resendOtp(@PathVariable String phoneNumber){
        log.debug("resending OTP to phone number: {}", phoneNumber);
        service.resendOtp(phoneNumber);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody ActivateRequest request){
        log.debug("logging in user with phone number: {}", request.getPhoneNumber());
        AuthResponse response = service.login(request.getPhoneNumber(), request.getOtp());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send/login/{phoneNumber}")
    public ResponseEntity<Void> sendLoginOtp( @PathVariable String phoneNumber) {
        log.debug("sending login OTP to phone number: {}", phoneNumber);
        service.sendLoginOtp(phoneNumber);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}