package github.oldLab.oldLab.serviceImpl;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import github.oldLab.oldLab.dto.request.ResetPasswordRequest;
import github.oldLab.oldLab.exception.UserAlreadyExistsException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import github.oldLab.oldLab.dto.request.LoginRequest;
import github.oldLab.oldLab.dto.request.PersonRequest;
import github.oldLab.oldLab.dto.response.AuthResponse;
import github.oldLab.oldLab.dto.response.PersonResponse;
import github.oldLab.oldLab.entity.Person;
import github.oldLab.oldLab.exception.InvalidTokenException;
import github.oldLab.oldLab.exception.NotImplementedException;
import github.oldLab.oldLab.exception.UserNotFoundException;
import github.oldLab.oldLab.repository.PersonRepository;
import github.oldLab.oldLab.service.PersonService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonServiceImpl implements PersonService {
    
    private final PersonRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenServiceImpl tokenService;
    private final ActivateServiceImpl activateService;

    @Qualifier("asyncExecutor")
    private final TaskExecutor taskExecutor;

    public void createAsync(PersonRequest personRequest) {
        log.info("creating person with first name: {}", personRequest.getFirstName());
        if (repository.existsByPhoneNumber(personRequest.getPhoneNumber())) {
            throw new UserAlreadyExistsException("Phone number " + personRequest.getPhoneNumber() + " already exists");
        }
        taskExecutor.execute(() -> {
            activateService.saveForRegister(personRequest.getPhoneNumber());
            personRequest.setPassword(passwordEncoder.encode(personRequest.getPassword()));
            repository.save(personRequest.toEntity());
            log.info("created person with first name: {}", personRequest.getFirstName());
        });
    }

    public AuthResponse authenticate(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getPhoneNumber(), request.getPassword()));
        var person = repository.findByPhoneNumber(request.getPhoneNumber())
            .orElseThrow(() -> new UserNotFoundException("user not found with phone number: " + request.getPhoneNumber()));
        CompletableFuture<String> token = tokenService.generateToken(person);
        return new AuthResponse(token.join(), PersonResponse.fromEntityToDto(person));
    }

    public PersonResponse findById(Long id) {
        log.info("finding person with id: {}", id);
        return PersonResponse.fromEntityToDto(repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("person not found with id: " + id)));
    }

    public PersonResponse findByPhoneNumber(String phoneNumber) {
        log.info("finding person with phone number: {}", phoneNumber);
        return PersonResponse.fromEntityToDto(repository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UserNotFoundException("person not found with phone number: " + phoneNumber))
        );
    }

    @Async("asyncExecutor")
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public CompletableFuture<PersonResponse> update(Long id, PersonRequest personRequest) {
        log.info("updating person with id: {}", id);
        return CompletableFuture.supplyAsync(() -> {
            Person person = repository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("person not found with id: " + id));
                    
            person.setFirstName(personRequest.getFirstName());
            person.setLastName(personRequest.getLastName());
            person.setPhoneNumber(personRequest.getPhoneNumber());
            person.setUpdatedAt(Instant.now());
            return PersonResponse.fromEntityToDto(repository.save(person));
        }, taskExecutor);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void delete(Long id) {
        log.info("deleting person with id: {}", id);
        Person person = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("person not found with id: " + id));
        repository.delete(person);
        log.info("deleted person with id: {}", id);
    }

    public void validateToken(String token) {
        token = token.substring(7);
        if(token == null || token.isEmpty()) {
            throw new InvalidTokenException("token is empty");
        }

        log.info("validating token: {}", token);
        if (!tokenService.isTokenValid(token,
                repository.findByPhoneNumber(tokenService.extractUsername(token)).orElseThrow(
                        () -> new UserNotFoundException("person not found")))) {
            throw new InvalidTokenException("token is invalid");
        }
    }

    public String getRole(String token) {
        token = token.substring(7);
        
        if(token == null || token.isEmpty()) {
            throw new InvalidTokenException("token is empty");
        }

        log.info("getting role from token: {}", token);
        Claims claim = tokenService.extractAllClaimsAsync(token).join();
        return claim.get("role", String.class);
    }

    public void updatePasswordAsync(LoginRequest loginRequest, String oldPassword) {
        log.info("updating password for phone number: {}", loginRequest.getPhoneNumber());
        taskExecutor.execute(() -> {
            Person person = repository.findByPhoneNumber(loginRequest.getPhoneNumber())
                .orElseThrow(() -> new UserNotFoundException("person not found with phone number: " + loginRequest.getPhoneNumber()));
            if (!passwordEncoder.matches(oldPassword, person.getPassword())) {
                throw new UserNotFoundException("incorrect current password for phone number: " + loginRequest.getPhoneNumber());
            }

            person.setPassword(passwordEncoder.encode(loginRequest.getPassword()));
            repository.save(person);
            log.info("updated password for phone number: {}", loginRequest.getPhoneNumber());
        });
    }

    public List<PersonResponse> getColleaguesAsync(String token, int page, int size) {
        log.info("getting colleagues for token: {}", token);
        if (token == null || token.isEmpty()) {
            throw new InvalidTokenException("token is empty");
        }

        final String actualToken = token.substring(7);
        Person person = repository.findByPhoneNumber(tokenService.extractUsername(actualToken))
            .orElseThrow(() -> new UserNotFoundException("invalid token: " + actualToken));

        return repository.findByCompanyId(person.getCompanyId(), PageRequest.of(page, size)).getContent().stream()
            .map(PersonResponse::fromEntityToDto)
            .toList();
    }

    public void sendOtp(String email){ //TODO: implement this method
        throw new NotImplementedException("sendOtp method by email is not implemented yet");
    }

    @Override
    public void requestPasswordReset(String contact) {
        boolean isEmail = contact.contains("@"); // Check is Email

        String normalizedContact = isEmail ? contact : normalizePhoneNumber(contact);

        Person person = isEmail
                ? repository.findByEmail(normalizedContact)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + contact))
                : repository.findByPhoneNumber(normalizedContact)
                .orElseThrow(() -> new UserNotFoundException("User not found with phone: " + contact));

        int otp = activateService.setOtp();

        activateService.saveOtpReset(person.getPhoneNumber(), otp, false);

        if (isEmail) {
            sendOtp(person.getEmail());
        } else {
            sendOtp(person.getPhoneNumber());
        }

        log.info("OTP sent to {}: {}", contact, otp);
    }
    private String normalizePhoneNumber(String phoneNumber) {
        // Удаляем все нецифровые символы и добавляем '+' в начале
        return "+" + phoneNumber.replaceAll("[^0-9]", "");
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Resetting password for: {}", request.getContact());

        activateService.validateOtpReset(request.getContact(), request.getOtpReset());

        Person person = request.isEmail()
                ? repository.findByEmail(request.getContact())
                .orElseThrow(() -> new UserNotFoundException("User not found"))
                : repository.findByPhoneNumber(request.getContact())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        person.setPassword(passwordEncoder.encode(request.getNewPassword()));
        person.setUpdatedAt(Instant.now());

        repository.save(person);
        log.info("Password reset successfully for: {}", request.getContact());
    }
}
