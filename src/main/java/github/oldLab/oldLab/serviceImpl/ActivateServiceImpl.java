package github.oldLab.oldLab.serviceImpl;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import github.oldLab.oldLab.Enum.MessageChannelEnum;
import github.oldLab.oldLab.exception.InvalidOtpException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import github.oldLab.oldLab.dto.request.ActivateRequest;
import github.oldLab.oldLab.dto.response.AuthResponse;
import github.oldLab.oldLab.dto.response.PersonResponse;
import github.oldLab.oldLab.entity.Activates;
import github.oldLab.oldLab.exception.UserNotFoundException;
import github.oldLab.oldLab.repository.ActivateRepository;
import github.oldLab.oldLab.repository.PersonRepository;
import github.oldLab.oldLab.service.ActivateService;
import github.oldLab.oldLab.service.RefreshTokenService;
import github.oldLab.oldLab.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivateServiceImpl implements ActivateService {

    private final ActivateRepository repository;
    private final PersonRepository personRepository;
    private final TokenService tokenService;
    private final MessageSenderServiceImpl messageSender;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.activation-ttl-minutes}")
    private int OTP_EXPIRATION_MINUTES;

    @Transactional
    public void setActive(ActivateRequest request) {
        log.debug("saving to activate with email: {}", request.getEmail());
        Activates activation = repository.findTopByEmailOrderByCreatedAtDesc(request.getEmail())
            .orElseThrow(() -> new UserNotFoundException("no OTP found for email: " + request.getEmail()));
        Instant createdAt = activation.getCreatedAt();
        Instant expiration = createdAt.plus(Duration.ofMinutes(OTP_EXPIRATION_MINUTES));
        if (Instant.now().isAfter(expiration)) {
            throw new UserNotFoundException(
                    "OTP expired for email: " + request.getEmail()
            );
        }

        if (activation.getOtp() != request.getOtp()) {
            throw new UserNotFoundException("invalid OTP for email: " + request.getEmail());
        }

        if (activation.isActive() == true) {
            throw new UserNotFoundException("user with email: " + request.getEmail() + " already activated");
        }

        delete(request.getEmail());

        personRepository.setActiveByEmail(request.getEmail(), true);
    }

    public int setOtp() {
        log.debug("generating otp");
        return 1000 + new Random().nextInt(9000);
    }
    
    public void sendOtp(String email) {
        messageSender.sendOtp(MessageChannelEnum.EMAIL, email, getOtp(email));
    }

    public void sendOtpReset(String email) {
        messageSender.sendOtp(MessageChannelEnum.EMAIL, email, getOtpReset(email));
    }

    public void save(String email, Optional<Boolean> isLogin) {
        log.debug("saving to activate with email: {}, loginAttempted={}", email, isLogin);
        int otp = setOtp();
        Instant createdAt = Instant.now();
        Activates activation = Activates.builder()
            .email(email)
            .otp(otp)
            .isActive(false)
            .isLogin(isLogin.orElse(false))
            .createdAt(createdAt)
            .build();
        repository.save(activation);
    }

    public void saveForRegister(String email) {
        save(email, Optional.ofNullable(null));
    }

    public void saveForLogin(String email) {
        save(email, Optional.of(true));
    }

    public int getOtp(String email) {
        log.debug("getting otp");
        return repository.findTopByEmailOrderByCreatedAtDesc(email)
            .orElseThrow(() -> new UserNotFoundException("no OTP found for email: " + email))
            .getOtp();
    }

    public int getOtpReset(String email) {
        log.debug("getting otp for reset");
        return repository.findTopByEmailOrderByCreatedAtDesc(email)
            .orElseThrow(() -> new UserNotFoundException("no OTP found for email: " + email))
            .getOtpReset();
    }

    public void resendOtp(String email) {
        log.debug("resending OTP to email: {}", email);
        Activates activation = repository.findTopByEmailOrderByCreatedAtDesc(email)
            .orElseThrow(() -> new UserNotFoundException("users with email: " + email + " not found, please register first"));

        Instant createdAt = activation.getCreatedAt();
        Instant expiration = createdAt.plus(Duration.ofMinutes(OTP_EXPIRATION_MINUTES));
        if (Instant.now().isAfter(expiration)) {
            saveForRegister(email);
        }
        messageSender.sendOtp(MessageChannelEnum.EMAIL,email, getOtp(email));
    }

    public AuthResponse login(String email, int OTP) {
        log.debug("logging by otp in user with email: {}", email);
        Activates activation = repository.findTopByEmailAndIsLoginOrderByCreatedAtDesc(email, true)
            .orElseThrow(() -> new UserNotFoundException("users with email: " + email + " not found, please send OTP first"));
        Instant createdAt = activation.getCreatedAt();
        Instant expiration = createdAt.plus(Duration.ofMinutes(OTP_EXPIRATION_MINUTES));
        if (Instant.now().isAfter(expiration)) {
            throw new UserNotFoundException("OTP expired for email: " + email);
        }
        if (activation.getOtp() != OTP) {
            throw new UserNotFoundException("invalid OTP for email: " + email);
        }

        delete(email);

        var person = personRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("person not found with email: " + email));
        CompletableFuture<String> token = tokenService.generateToken(person);
        String refreshToken = refreshTokenService.issue(person);
        return new AuthResponse(token.join(), refreshToken, PersonResponse.fromEntityToDto(person));
    }

    public void delete(String email) {
        log.debug("deleting activation with email: {}", email);
        Activates activation = repository.findTopByEmailOrderByCreatedAtDesc(email)
            .orElseThrow(() -> new UserNotFoundException("activation with email: " + email + " not found"));
        repository.delete(activation);
    }

    public void sendLoginOtp(String email) {
        log.debug("sending login OTP to email: {}", email);
        saveForLogin(email);
        messageSender.sendOtp(MessageChannelEnum.EMAIL,email, getOtp(email));
    }

    // Methods for reset password
    public void saveOtpReset(String email, int otp, boolean isForLogin) {
    Activates activate = repository.findTopByEmailOrderByCreatedAtDesc(email)
        .orElse(new Activates());
        activate.setEmail(email);
        activate.setOtpReset(otp);
        activate.setActive(true);
        activate.setLogin(isForLogin);
        activate.setCreatedAt(Instant.now());

        repository.save(activate);
    }

    @Transactional
    public void validateOtpReset(String email, int otp) {
        Activates activate = repository.findByEmailAndOtpResetAndIsActive(email, otp, true)
                .orElseThrow(() -> new InvalidOtpException("Invalid OTP"));

        Instant createdAt = activate.getCreatedAt();
        Instant expiration = createdAt.plus(Duration.ofMinutes(OTP_EXPIRATION_MINUTES));
        if (Instant.now().isAfter(expiration)) {
            throw new InvalidOtpException("OTP expired");
        }
        activate.setActive(false);
        repository.save(activate);
    }
    
    @Transactional
    public void cleanupOldRecords() {
        Instant cutoffDate = Instant.now().minusSeconds(60 * 60 * 24 + 60 * OTP_EXPIRATION_MINUTES); //60sec * 60min * 24hour + minutes in .env || .yaml (converted to seconds)
        repository.deleteOlderThan(cutoffDate);
    }
}