package tech.github.oldlabclient.service;

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
import tech.github.oldlabclient.dto.request.PersonRequest;
import tech.github.oldlabclient.dto.response.PersonResponse;
import tech.github.oldlabclient.entity.Person;
import tech.github.oldlabclient.exception.UserNotFoundException;
import tech.github.oldlabclient.repository.PersonRepository;
import tech.github.oldlabclient.repository.PhotoRepository;
import tech.github.oldlabclient.service.PersonService;

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
        log.info("creating person with first name: {}", personRequest.getFirstName());
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
        log.info("finding person with id: {}", id);
        return PersonResponse.fromEntity(repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("person not found with id: " + id)));
    }

    public Person findEntityById(UUID id) {
        log.info("finding person with id: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("person not found with id: " + id));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    // @org.springframework.cache.annotation.CacheEvict(
    //     value = {"personByEmail", "personById", "personId"},
    //     key = "#id"
    // )
    public PersonResponse update(UUID id, PersonRequest personRequest) {
        log.info("updating person with id: {}", id);
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

    public boolean existsById(UUID id) {
        log.info("checking if person exists with id: {}", id);
        return repository.existsById(id);
    }

    public Person getReferenceById(UUID id) {
        log.info("getting reference for person with id: {}", id);
        return repository.getReferenceById(id);
    }

    public Person getReferenceByIdIfExists(UUID id) {
        if (!repository.existsById(id)) {
            throw new UserNotFoundException("user not found with id: " + id);
        }
        return repository.getReferenceById(id);
    }
}