package github.oldLab.oldLab.serviceImpl;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import github.oldLab.oldLab.exception.InvalidOtpException;
import org.springframework.stereotype.Service;

import github.oldLab.oldLab.dto.request.ActivateRequest;
import github.oldLab.oldLab.dto.response.AuthResponse;
import github.oldLab.oldLab.dto.response.PersonResponse;
import github.oldLab.oldLab.entity.Activate;
import github.oldLab.oldLab.exception.UserNotFoundException;
import github.oldLab.oldLab.repository.ActivateRepository;
import github.oldLab.oldLab.repository.PersonRepository;
import github.oldLab.oldLab.service.ActivateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivateServiceImpl implements ActivateService {

    private final ActivateRepository repository;
    private final PersonRepository personRepository;
    private final TokenServiceImpl tokenService;

    private final int OTP_EXPIRATION_MINUTES = 15;

    public void setActive(ActivateRequest request) {
        log.debug("saving to activate with phone number: {}", request.getPhoneNumber());
        Activate activation = repository.findByPhoneNumber(request.getPhoneNumber())
            .orElseThrow(() -> new UserNotFoundException("no OTP found for phone number: " + request.getPhoneNumber()));

        if (activation.getCreatedAt().plusMinutes(OTP_EXPIRATION_MINUTES).isAfter(LocalDateTime.now())) {
            throw new UserNotFoundException("OTP expired for phone number: " + request.getPhoneNumber());
        }

        if (activation.getOtp() != request.getOtp()) {
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
        return new Random().nextInt(9999);
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

        sendOtp(phoneNumber, getOtp(phoneNumber)); //here
    }

    public AuthResponse login(String phoneNumber, int OTP) {
        log.debug("logging by otp in user with phone number: {}", phoneNumber);
        Activate activation = repository.findByPhoneNumberAndIsLogin(phoneNumber, true)
            .orElseThrow(() -> new UserNotFoundException("users with phone number: " + phoneNumber + " not found, please send OTP first"));
        if (activation.getCreatedAt().plusMinutes(OTP_EXPIRATION_MINUTES).isBefore(LocalDateTime.now())) {
            throw new UserNotFoundException("OTP expired for phone number: " + phoneNumber);
        }

        if (activation.getOtp() != OTP) {
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

    // Methods for reset password
    public void saveOtpReset(String phoneNumber, int otp, boolean isForLogin) {
        repository.findByPhoneNumber(phoneNumber).ifPresentOrElse(
                existing -> repository.save(
                        Activate.builder()
                                .id(existing.getId())
                                .phoneNumber(phoneNumber)
                                .otpReset(otp)
                                .isActive(true)
                                .isLogin(isForLogin)
                                .createdAt(LocalDateTime.now())
                                .build()
                ),
                () -> repository.save(
                        Activate.builder()
                                .phoneNumber(phoneNumber)
                                .otpReset(otp)
                                .isActive(true)
                                .isLogin(isForLogin)
                                .createdAt(LocalDateTime.now())
                                .build()
                )
        );
    }

    public void validateOtpReset(String phoneNumber, String otp) {
        Activate activate = repository.findByPhoneNumberAndOtpResetAndIsActive(phoneNumber, Integer.parseInt(otp), true)
                .orElseThrow(() -> new InvalidOtpException("Invalid OTP"));

        if (activate.getCreatedAt().plusMinutes(5).isBefore(LocalDateTime.now())) {
            throw new InvalidOtpException("OTP expired");
        }

        // Деактивируем OTP после использования
        activate.setActive(false);
        repository.save(activate);
    }
    // End of Section

    // Cleanup Method
    @Transactional
    public void cleanupOldRecords() {
        // Удаляем записи старше 15 минут (TTL для otp)
        LocalDateTime cutoffDate = LocalDateTime.now(ZoneOffset.UTC).minusMinutes(15);
        repository.deleteOlderThan(cutoffDate);
    }
}