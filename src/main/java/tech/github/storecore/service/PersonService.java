package tech.github.storecore.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.github.storecore.dto.request.PersonRequest;
import tech.github.storecore.dto.response.PersonResponse;
import tech.github.storecore.entity.Person;
import tech.github.storecore.exception.UserNotFoundException;
import tech.github.storecore.repository.PersonRepository;
import tech.github.storecore.repository.PhotoRepository;
import tech.github.storecore.service.PersonService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonService{

    private final PersonRepository repository;
    private final PhotoRepository photoRepository;
    private final MinioPhotoStorage photoStorage;

    @Value("${app.inactive-account-ttl-days}")
    private int inactiveAccountTtlDays;

    @Qualifier("asyncExecutor")
    private final TaskExecutor taskExecutor;

    public void create(PersonRequest personRequest) {
        // if (repository.existsByEmail(personRequest.getEmail())) {
        // throw new UserAlreadyExistsException("email " + personRequest.getEmail() + "
        // already exists");
        // }
        // if (repository.existsByEmail(personRequest.getEmail())) {
        // return;
        // }
        // activateService.saveForRegister(personRequest.getEmail());
        // TODO: call OL_Auth to ensure email uniqueness across services
        repository.save(personRequest.toEntity());
        log.debug("created person with first name: {}", personRequest.getFirstName());
    }

    public PersonResponse findById(UUID id) {
        return PersonResponse.fromEntity(repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("person not found with id: " + id)));
    }

    public Person findEntityById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("person not found with id: " + id));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    // @org.springframework.cache.annotation.CacheEvict(
    //     value = {"personByEmail", "personById", "personId"},
    //     key = "#id"
    // )
    public PersonResponse update(UUID id, PersonRequest personRequest) {
            Person person = getReferenceByIdIfExists(id);

            person.setFirstName(personRequest.getFirstName());
            person.setLastName(personRequest.getLastName());
            person.setPhoneNumber(personRequest.getPhoneNumber());
            person.setUpdatedAt(Instant.now());
            return PersonResponse.fromEntity(repository.save(person));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    // @org.springframework.cache.annotation.CacheEvict(
    //     value = {"personByEmail", "personById", "personId"},
    //     key = "#id"
    // )
    public void delete(UUID id) { //TODO: call OL_Auth to delete associated auth data
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
    }

    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    public Person getReferenceById(UUID id) {
        return repository.getReferenceById(id);
    }

    public Person getReferenceByIdIfExists(UUID id) {
        if (!repository.existsById(id)) {
            throw new UserNotFoundException("user not found with id: " + id);
        }
        return repository.getReferenceById(id);
    }
}