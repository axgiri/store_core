package github.oldLab.oldLab.serviceImpl;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import github.oldLab.oldLab.dto.request.ResetPasswordRequest;
import github.oldLab.oldLab.exception.UserAlreadyExistsException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.PageRequest;
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
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final ActivateService activateService;
    private final RefreshTokenService refreshTokenService;

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

    public Person findEntityByPhoneNumber(String phoneNumber) {
        log.info("finding person with phone number: {}", phoneNumber);
        return repository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UserNotFoundException("person not found with phone number: " + phoneNumber));
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public PersonResponse update(Long id, PersonRequest personRequest) {
        log.info("updating person with id: {}", id);
            Person person = repository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("person not found with id: " + id));

            person.setFirstName(personRequest.getFirstName());
            person.setLastName(personRequest.getLastName());
            person.setPhoneNumber(personRequest.getPhoneNumber());
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
                photoStorage.delete(photo.getObjectKey());
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
                repository.findByPhoneNumber(tokenService.extractUsername(token)).orElseThrow(
                        () -> new UserNotFoundException("person not found")))) {
            throw new InvalidTokenException("token is invalid");
        }
    }

    public CompletableFuture<Void> updatePasswordAsync(LoginRequest loginRequest, String oldPassword) {
        log.info("updating password for phone number: {}", loginRequest.getPhoneNumber());
        return CompletableFuture.runAsync(() -> {
            Person person = repository.findByPhoneNumber(loginRequest.getPhoneNumber())
                    .orElseThrow(() -> new UserNotFoundException("person not found with phone number: " + loginRequest.getPhoneNumber()));
            if (!passwordEncoder.matches(oldPassword, person.getPassword())) {
                throw new UserNotFoundException("incorrect current password for phone number: " + loginRequest.getPhoneNumber());
            }

            person.setPassword(passwordEncoder.encode(loginRequest.getPassword()));
            repository.save(person);
            log.info("updated password for phone number: {}", loginRequest.getPhoneNumber());
        }, taskExecutor);
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

    public void sendOtp(String email){
        throw new NotImplementedException("sendOtp method by email is not implemented yet");
    }

    @Override
    public void requestPasswordReset(String contact) {
        boolean isEmail = contact.contains("@") && contact.contains("."); // Check is Email

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
            activateService.sendOtp(person.getPhoneNumber());
        }

        log.info("OTP sent to {}: {}", contact, otp);
    }
    private String normalizePhoneNumber(String phoneNumber) {
        // Remove all non-numeric characters and add '+' at the beginning
        return "+" + phoneNumber.replaceAll("[^0-9]", "");
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

    public Long getIdFromPhoneNumber(String phoneNumber) {
        log.info("getting id for user with phone number: {}", phoneNumber);
        return repository.findIdByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UserNotFoundException("user not found with phone number: " + phoneNumber));
    }

    public Long getCompanyIdByPersonId(Long personId) {
        log.info("getting company id for person with id: {}", personId);
        return repository.findCompanyIdById(personId)
                .orElseThrow(() -> new UserNotFoundException("company not found for person with id: " + personId));
    }

    public void setCompanyIdForExistingPerson(Long personId, Long companyId) {
        log.info("setting company id for person with id: {}", personId);
        Person person = repository.findById(personId)
                .orElseThrow(() -> new UserNotFoundException("user not found with id: " + personId));
        person.setCompanyId(companyId);
        person.setUpdatedAt(Instant.now());
        repository.save(person);
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
}