package github.oldLab.oldLab.service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.core.task.TaskExecutor;
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
import github.oldLab.oldLab.exception.InvalidPasswordException;
import github.oldLab.oldLab.exception.InvalidTokenException;
import github.oldLab.oldLab.exception.UserNotFoundException;
import github.oldLab.oldLab.repository.PersonRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonService {
    
    private final PersonRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final ActivateService activateService;
    private final TaskExecutor taskExecutor;

    public PersonResponse create(PersonRequest personRequest) {
        log.info("creating person with first name: {}", personRequest.getFirstName());
        checkPhoneNumberUnique(personRequest.getPhoneNumber());
        personRequest.setPassword(passwordEncoder.encode(personRequest.getPassword()));
        activateService.saveForRegister(personRequest.getPhoneNumber());
        return PersonResponse.fromEntityToDto(repository.save(personRequest.toEntity()));
    }

    public void createAsync(PersonRequest personRequest) {
        log.info("creating person with first name: {}", personRequest.getFirstName());
        taskExecutor.execute(() -> {
            checkPhoneNumberUnique(personRequest.getPhoneNumber());
            activateService.saveForRegister(personRequest.getPhoneNumber());
            personRequest.setPassword(passwordEncoder.encode(personRequest.getPassword()));
            repository.save(personRequest.toEntity());
            log.info("created person with first name: {}", personRequest.getFirstName());
        });
    }

    public AuthResponse authenticate(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getPhoneNumber(), request.getPassword()));
        var person = getPersonByPhoneNumber(request.getPhoneNumber());
        CompletableFuture<String> token = tokenService.generateToken(person);
        return new AuthResponse(token.join(), PersonResponse.fromEntityToDto(person));
    }

    public PersonResponse findById(Long id) {
        log.info("finding person with id: {}", id);
        return PersonResponse.fromEntityToDto(getPersonById(id));
    }

    public PersonResponse findByPhoneNumber(String phoneNumber) {
        log.info("finding person with phone number: {}", phoneNumber);
        return PersonResponse.fromEntityToDto(getPersonByPhoneNumber(phoneNumber));
    }

    @Async("asyncExecutor")
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public CompletableFuture<PersonResponse> update(Long id, PersonRequest personRequest) {
        log.info("updating person with id: {}", id);
        return CompletableFuture.supplyAsync(() -> {
            Person person = getPersonById(id);
            updatePersonFields(person, personRequest);
            return PersonResponse.fromEntityToDto(repository.save(person));
        }, taskExecutor);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void delete(Long id) {
        log.info("deleting person with id: {}", id);
        Person person = getPersonById(id);
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
                getPersonByPhoneNumber(tokenService.extractUsername(token)))) {
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
            Person person = getPersonByPhoneNumber(loginRequest.getPhoneNumber());
            if (!passwordEncoder.matches(oldPassword, person.getPassword())) {
                throw new InvalidPasswordException("incorrect current password for phone number: " + loginRequest.getPhoneNumber());
            }
            person.setPassword(passwordEncoder.encode(loginRequest.getPassword()));
            repository.save(person);
            log.info("updated password for phone number: {}", loginRequest.getPhoneNumber());
        });
    }

    public List<PersonResponse> getColleaguesAsync(String token) {
        log.info("getting colleagues for token: {}", token);
        if (token == null || token.isEmpty()) {
            throw new InvalidTokenException("token is empty");
        }
        final String actualToken = token.substring(7);
        Person person = getPersonByPhoneNumber(tokenService.extractUsername(actualToken));
        return repository.findByCompanyId(person.getCompanyId()).stream()
            .map(PersonResponse::fromEntityToDto)
            .toList();
    }

    private void checkPhoneNumberUnique(String phoneNumber) {
        if (repository.findByPhoneNumber(phoneNumber).isPresent()) {
            throw new UserNotFoundException("user with this phone number already exists: " + phoneNumber);
        }
    }

    private Person getPersonByPhoneNumber(String phoneNumber) {
        return repository.findByPhoneNumber(phoneNumber)
            .orElseThrow(() -> new UserNotFoundException("person not found with phone number: " + phoneNumber));
    }

    private Person getPersonById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("person not found with id: " + id));
    }

    private void updatePersonFields(Person person, PersonRequest personRequest) {
        person.setFirstName(personRequest.getFirstName());
        person.setLastName(personRequest.getLastName());
        person.setPhoneNumber(personRequest.getPhoneNumber());
        person.setUpdatedAt(LocalDate.now());
    }
}
