package tech.github.storecore.service;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.uuid.Generators;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.github.storecore.client.StoreAuthClient;
import tech.github.storecore.dto.request.PersonCreateRequest;
import tech.github.storecore.dto.request.PersonRequest;
import tech.github.storecore.dto.response.PersonResponse;
import tech.github.storecore.entity.Person;
import tech.github.storecore.exception.UserAlreadyExistsException;
import tech.github.storecore.exception.UserNotFoundException;
import tech.github.storecore.repository.PersonRepository;
import tech.github.storecore.repository.PhotoRepository;
import tech.github.storecore.repository.ProductRepository;
import tech.github.storecore.search.ProductSearchRepository;
import tech.github.storecore.service.PersonService;
import tech.github.storecore.service.saga.PersonProducer;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonService{

    private final PersonRepository repository;
    private final PhotoRepository photoRepository;
    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;
    private final MinioPhotoStorage photoStorage;
    private final StoreAuthClient storeAuthClient;
    private final PersonProducer personProducer;

    @Value("${app.inactive-account-ttl-days}")
    private int inactiveAccountTtlDays;

    @Qualifier("asyncExecutor")
    private final TaskExecutor taskExecutor;

    @Transactional
    public void create(PersonCreateRequest personCreateRequest) {
        if (!storeAuthClient.validateEmail(personCreateRequest.email())) {
            throw new UserAlreadyExistsException("user already exists with email: " + personCreateRequest.email());
        }

        UUID personId = Generators.timeBasedEpochGenerator().generate();
        personProducer.sendCreateUserEvent(personCreateRequest, personId);

        repository.save(
            Person.builder()
                .id(personId)
                .firstName(personCreateRequest.firstName())
                .lastName(personCreateRequest.lastName())
                .phoneNumber(personCreateRequest.phoneNumber())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build()
            );
        log.debug("created person with id: {}, first name: {} and last name: {}", personId, personCreateRequest.firstName(), personCreateRequest.lastName());
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
    public void delete(UUID id) {
        Person person = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("person not found with id: " + id));

        productRepository.findAllByPersonId(id).forEach(product -> {
            photoRepository.findAllByProductId(product.getId()).forEach(photo -> {
                try {
                    photoStorage.delete(photo.getObjectKey(), photo.getBucket());
                } catch (Exception e) {
                    log.warn("failed to delete product photo '{}' from storage: {}", photo.getObjectKey(), e.getMessage());
                }
                photoRepository.delete(photo);
            });
            productSearchRepository.deleteById(product.getId());
            productRepository.delete(product);
        });

        photoRepository.findByPersonId(id).ifPresent(photo -> {
            try {
                photoStorage.delete(photo.getObjectKey(), photo.getBucket());
            } catch (Exception e) {
                log.warn("failed to delete person photo '{}' from storage: {}", photo.getObjectKey(), e.getMessage());
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