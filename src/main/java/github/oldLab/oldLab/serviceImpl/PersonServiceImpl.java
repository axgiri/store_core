package github.oldLab.oldLab.serviceImpl;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import github.oldLab.oldLab.dto.request.ResetPasswordRequest;
import github.oldLab.oldLab.entity.Person;
import github.oldLab.oldLab.exception.UserAlreadyExistsException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import github.oldLab.oldLab.Enum.RoleEnum;
import github.oldLab.oldLab.dto.request.ContactRequest;
import github.oldLab.oldLab.dto.request.LoginRequest;
import github.oldLab.oldLab.dto.request.PersonRequest;
import github.oldLab.oldLab.dto.request.UpdatePasswordRequest;
import github.oldLab.oldLab.dto.response.AuthResponse;
import github.oldLab.oldLab.dto.response.PersonResponse;
import github.oldLab.oldLab.exception.InvalidTokenException;
import github.oldLab.oldLab.exception.UserNotFoundException;
import github.oldLab.oldLab.repository.PersonRepository;
import github.oldLab.oldLab.repository.PhotoRepository;
import github.oldLab.oldLab.service.PhotoStorage;
import github.oldLab.oldLab.service.ActivateService;
import github.oldLab.oldLab.service.PersonService;
import github.oldLab.oldLab.service.RefreshTokenService;
import github.oldLab.oldLab.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonServiceImpl implements PersonService {

    private final PersonRepository repository;
    private final PhotoRepository photoRepository;
    private final PhotoStorage photoStorage;
    private final PasswordEncoder passwordEncoder;
    private final ObjectProvider<AuthenticationManager> authenticationManagerProvider;
    private final TokenService tokenService;
    private final ActivateService activateService;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.inactive-account-ttl-days}")
    private int inactiveAccountTtlDays;

    @Qualifier("asyncExecutor")
    private final TaskExecutor taskExecutor;

    public void createAsync(PersonRequest personRequest) {
        log.info("creating person with first name: {}", personRequest.getFirstName());
        if (repository.existsByEmail(personRequest.getEmail())) {
            throw new UserAlreadyExistsException("email " + personRequest.getEmail() + " already exists");
        }
        taskExecutor.execute(() -> {
            activateService.saveForRegister(personRequest.getEmail());
            personRequest.setPassword(passwordEncoder.encode(personRequest.getPassword()));
            repository.save(personRequest.toEntity());
            log.info("created person with first name: {}", personRequest.getFirstName());
        });
    }

    public AuthResponse authenticate(LoginRequest request) {
        authenticationManagerProvider.getObject()
                .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        var person = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("user not found with email: " + request.getEmail()));
        CompletableFuture<String> token = tokenService.generateToken(person);
        String refreshToken = refreshTokenService.issue(person);
        return new AuthResponse(token.join(), refreshToken, PersonResponse.fromEntityToDto(person));
    }

    public AuthResponse refreshAccessToken(String refreshToken) {
        var rotated = refreshTokenService.rotate(refreshToken);
        var person = rotated.getPerson();
        CompletableFuture<String> access = tokenService.generateToken(person);
        return new AuthResponse(access.join(), rotated.getTokenHash(), PersonResponse.fromEntityToDto(person));
    }

    public void revoke(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    public void revokeAll(String refreshToken) {
        refreshTokenService.revokeAllForPerson(refreshToken);
    }

    public PersonResponse findById(Long id) {
        log.info("finding person with id: {}", id);
        return PersonResponse.fromEntityToDto(repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("person not found with id: " + id)));
    }

    public Person findEntityById(Long id) {
        log.info("finding person with id: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("person not found with id: " + id));
    }

    public PersonResponse findByPhoneNumber(String phoneNumber) {
        log.info("finding person with phone number: {}", phoneNumber);
        return PersonResponse.fromEntityToDto(repository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UserNotFoundException("person not found with phone number: " + phoneNumber))
        );
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public PersonResponse update(Long id, PersonRequest personRequest) {
        log.info("updating person with id: {}", id);
            Person person = getReferenceByIdIfExists(id);

            // Will throw BadCredentialsException on mismatch and let controller advice map it to 401
            verifyPassword(personRequest.getPassword(), person.getPassword());

            person.setFirstName(personRequest.getFirstName());
            person.setLastName(personRequest.getLastName());
            person.setPhoneNumber(personRequest.getPhoneNumber());
            person.setEmail(personRequest.getEmail());
            person.setUpdatedAt(Instant.now());
            return PersonResponse.fromEntityToDto(repository.save(person));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void delete(Long id) {
        log.info("deleting person with id: {}", id);
        Person person = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("person not found with id: " + id));
        photoRepository.findByPersonId(id).ifPresent(photo -> {
            try {
                photoStorage.delete(photo.getObjectKey(), photo.getBucket());
            } catch (Exception e) {
                log.warn("failed to delete photo object '{}' from storage: {}", photo.getObjectKey(), e.getMessage());
                throw e;
            }
            photoRepository.delete(photo);
        });

        repository.delete(person);
        log.info("deleted person with id: {}", id);
    }

    public void validateToken(String token) {
        token = token.substring(7);
        if(token == null || token.isEmpty()) {
            throw new InvalidTokenException("token is empty");
        }

        if (!tokenService.isTokenValid(token,
                repository.findByEmail(tokenService.extractUsername(token)).orElseThrow(
                        () -> new UserNotFoundException("person not found")))) {
            throw new InvalidTokenException("token is invalid");
        }
    }

    public CompletableFuture<Void> updatePasswordAsync(UpdatePasswordRequest request) {
        log.info("updating password for email: {}", request.getEmail());
        return CompletableFuture.runAsync(() -> {
            Person person = repository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UserNotFoundException("person not found with email: " + request.getEmail()));
            verifyPassword(request.getOldPassword(), person.getPassword());

            person.setPassword(passwordEncoder.encode(request.getNewPassword()));
            repository.save(person);
            log.info("updated password for email: {}", request.getEmail());
        }, taskExecutor);
    }

    @Override
    public void requestPasswordReset(ContactRequest contactRequest) {

        Person person = repository.findByEmail(contactRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + contactRequest.getEmail()));

        int otp = activateService.setOtp();
        activateService.saveOtpReset(person.getEmail(), otp, false);
        activateService.sendOtpReset(person.getEmail());
        log.info("OTP sent to {}: {}", contactRequest.getEmail(), otp);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Resetting password for: {}", request.getContact());

        activateService.validateOtpReset(request.getContact(), request.getOtpReset());

        Person person = request.isEmail()
                ? repository.findByEmail(request.getContact())
                .orElseThrow(() -> new UserNotFoundException("user not found with email: " + request.getContact()))
                : repository.findByPhoneNumber(request.getContact())
                .orElseThrow(() -> new UserNotFoundException("user not found with phone: " + request.getContact()));

        person.setPassword(passwordEncoder.encode(request.getNewPassword()));
        person.setUpdatedAt(Instant.now());

        repository.save(person);
        log.info("Password reset successfully for: {}", request.getContact());
    }

    public Long getIdFromEmail(String email) {
        log.info("getting id for user with email: {}", email);
        return repository.findIdByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("user not found with email: " + email));
    }

    public boolean existsById(Long id) {
        log.info("checking if person exists with id: {}", id);
        return repository.existsById(id);
    }

    public Person getReferenceById(Long id) {
        log.info("getting reference for person with id: {}", id);
        return repository.getReferenceById(id);
    }

    public Person getReferenceByIdIfExists(Long id) {
        if (!repository.existsById(id)) {
            throw new UserNotFoundException("user not found with id: " + id);
        }
        return repository.getReferenceById(id);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Person upsertFromOAuth(String email, String firstName, String lastName) {
        final String safeFirst = firstName != null ? firstName : "";
        final String safeLast = lastName != null ? lastName : "";

        Person person = repository.findByEmail(email).orElse(null);
        if (person == null) {
            Person newPerson = new Person();
            newPerson.setEmail(email);
            newPerson.setFirstName(safeFirst);
            newPerson.setLastName(safeLast);
            newPerson.setIsActive(true);
            newPerson.setRoleEnum(RoleEnum.USER);
            newPerson.setCreatedAt(Instant.now());
            newPerson.setUpdatedAt(Instant.now());
            newPerson.setPassword(UUID.randomUUID().toString());
            return repository.save(newPerson);
        } else {
            switch (firstName) {
                case null -> {}
                case String s when s.isBlank() -> {}
                default -> person.setFirstName(firstName);
            }

            switch (lastName) {
                case null -> {} //killer feature of jre 21 xd. no NPE here
                case String s when s.isBlank() -> {}
                default -> person.setLastName(lastName);
            }

            person.setUpdatedAt(Instant.now());
            
            return repository.save(person);
        }
    }

    private void verifyPassword(String raw, String encoded) {
        if (!passwordEncoder.matches(raw, encoded)) {
            throw new BadCredentialsException("invalid credentials");
        }
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void cleanupInactivePersons() {
        Instant cutoffDate = Instant.now().minusSeconds(60L * 60 * 24 * inactiveAccountTtlDays); //60sec * 60min * 24hour * days in .application
        repository.deleteByIsActiveFalseAndCreatedAtBefore(cutoffDate);
    }
}