package github.oldLab.oldLab.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import github.oldLab.oldLab.dto.request.ActivateRequest;
import github.oldLab.oldLab.dto.response.AuthResponse;
import github.oldLab.oldLab.dto.response.PersonResponse;
import github.oldLab.oldLab.entity.Activate;
import github.oldLab.oldLab.exception.UserNotFoundException;
import github.oldLab.oldLab.repository.ActivateRepository;
import github.oldLab.oldLab.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivateService {

    private final ActivateRepository repository;
    private final PersonRepository personRepository;
    private final TokenService tokenService;

    private final int OTP_EXPIRATION_MINUTES = 15;

    public void setActive(ActivateRequest request) {
        log.debug("saving to activate with phone number: {}", request.getPhoneNumber());
        Activate activation = repository.findByPhoneNumber(request.getPhoneNumber())
            .orElseThrow(() -> new UserNotFoundException("no OTP found for phone number: " + request.getPhoneNumber()));

        if (activation.getCreatedAt().plusMinutes(OTP_EXPIRATION_MINUTES).isAfter(LocalDateTime.now())) {
            throw new UserNotFoundException("OTP expired for phone number: " + request.getPhoneNumber());
        }

        int otpInt;
        try {
            otpInt = Integer.parseInt(request.getOtp());
        } catch (NumberFormatException e) {
            throw new UserNotFoundException("OTP must be a number for phone number: " + request.getPhoneNumber());
        }
        if (activation.getOtp() != otpInt) {
            throw new UserNotFoundException("invalid OTP for phone number: " + request.getPhoneNumber());
        }

        if (activation.isActive() == true) {
            throw new UserNotFoundException("user with phone number: " + request.getPhoneNumber() + " already activated");
        }

        delete(request.getPhoneNumber());

        repository.setActiveByPhoneNumber(request.getPhoneNumber(), true);
    }

    public int setOtp() {
        log.debug("generating otp");
        return new Random().nextInt(9000) + 1000;
    }

    public void save(String phoneNumber, Optional<Boolean> isLogin) {
        log.debug("saving to activate with phone number: {}, loginAttempted={}", phoneNumber, isLogin);
        int otp = setOtp();
        LocalDateTime createdAt = LocalDateTime.now();
        Activate activation = Activate.builder()
            .phoneNumber(phoneNumber)
            .otp(otp)
            .isActive(false)
            .isLogin(isLogin.orElse(null))
            .createdAt(createdAt)
            .build();
        repository.save(activation);
    }

    public void saveForRegister(String phoneNumber) {
        save(phoneNumber, Optional.of(null));
    }

    public void saveForLogin(String phoneNumber) {
        save(phoneNumber, Optional.of(true));
    }

    public int getOtp(String phoneNumber) {
        log.debug("getting otp");
        return repository.findByPhoneNumber(phoneNumber)
            .orElseThrow(() -> new UserNotFoundException("no OTP found for phone number: " + phoneNumber))
            .getOtp();
    }

    public void sendOtp(String phoneNumber) {
        log.debug("sending OTP to phone number: {}", phoneNumber);
        int otp = getOtp(phoneNumber);
        sendOtp(phoneNumber, otp);
    }

    public void sendOtp(String phoneNumber, int otp) {
        log.debug("sending OTP to phone number: {}", phoneNumber);
        //TODO: send OTP to another service by kafka
    }

    public void resendOtp(String phoneNumber) {
        log.debug("resending OTP to phone number: {}", phoneNumber);
        Activate activation = repository.findByPhoneNumber(phoneNumber)
            .orElseThrow(() -> new UserNotFoundException("users with phone number: " + phoneNumber + " not found, please register first"));

        if (activation.getCreatedAt().plusMinutes(OTP_EXPIRATION_MINUTES).isBefore(LocalDateTime.now())) {
            saveForRegister(phoneNumber);
        }

        sendOtp(phoneNumber, getOtp(phoneNumber));
    }

    public AuthResponse login(String phoneNumber, String otp) {
        log.debug("logging by otp in user with phone number: {}", phoneNumber);
        Activate activation = repository.findByPhoneNumberAndIsLogin(phoneNumber, true)
            .orElseThrow(() -> new UserNotFoundException("users with phone number: " + phoneNumber + " not found, please send OTP first"));
        if (activation.getCreatedAt().plusMinutes(OTP_EXPIRATION_MINUTES).isBefore(LocalDateTime.now())) {
            throw new UserNotFoundException("OTP expired for phone number: " + phoneNumber);
        }

        int otpInt;
        try {
            otpInt = Integer.parseInt(otp);
        } catch (NumberFormatException e) {
            throw new UserNotFoundException("OTP must be a number for phone number: " + phoneNumber);
        }
        if (activation.getOtp() != otpInt) {
            throw new UserNotFoundException("invalid OTP for phone number: " + phoneNumber);
        }

        if (activation.isActive() == false) {
            throw new UserNotFoundException("user with phone number: " + phoneNumber + " not activated yet, please activate first");
        }

        delete(phoneNumber);

        var person = personRepository.findByPhoneNumber(phoneNumber)
            .orElseThrow(() -> new UserNotFoundException("user not found with phone number: " + phoneNumber));
        CompletableFuture<String> token = tokenService.generateToken(person);
        return new AuthResponse(token.join(), PersonResponse.fromEntityToDto(person));
    }

    public void delete(String phoneNumber) {
        log.debug("deleting activation with phone number: {}", phoneNumber);
        Activate activation = repository.findByPhoneNumber(phoneNumber)
            .orElseThrow(() -> new UserNotFoundException("activation with phone number: " + phoneNumber + " not found"));
        repository.delete(activation);
    }

    public void sendLoginOtp(String phoneNumber) {
        log.debug("sending login OTP to phone number: {}", phoneNumber);
        saveForLogin(phoneNumber);
        sendOtp(phoneNumber);
    }
}