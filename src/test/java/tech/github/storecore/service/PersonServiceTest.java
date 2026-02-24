package tech.github.storecore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;

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
import tech.github.storecore.service.saga.PersonProducer;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock private PersonRepository repository;
    @Mock private PhotoRepository photoRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ProductSearchRepository productSearchRepository;
    @Mock private MinioPhotoStorage photoStorage;
    @Mock private StoreAuthClient storeAuthClient;
    @Mock private PersonProducer personProducer;
    @Mock private TaskExecutor taskExecutor;

    @InjectMocks
    private PersonService personService;

    private Person testPerson(UUID id) {
        return Person.builder()
                .id(id)
                .firstName("Alex")
                .lastName("Smith")
                .phoneNumber("+1234567890")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("validates email, sends event, and saves person")
        void createsSuccessfully() {
            var req = new PersonCreateRequest("AA", "BB", "a@t.com", "password");
            when(storeAuthClient.validateEmail("a@t.com")).thenReturn(true);

            personService.create(req);

            verify(personProducer).sendCreateUserEvent(eq(req), any(UUID.class));
            verify(repository).save(any(Person.class));
        }

        @Test
        @DisplayName("throws UserAlreadyExistsException when email not valid")
        void throws_whenEmailTaken() {
            var req = new PersonCreateRequest("A", "B", "taken@t.com", "pw");
            when(storeAuthClient.validateEmail("taken@t.com")).thenReturn(false);

            assertThatThrownBy(() -> personService.create(req))
                    .isInstanceOf(UserAlreadyExistsException.class);

            verify(repository, never()).save(any());
        }

        private static <T> T eq(T value) {
            return org.mockito.ArgumentMatchers.eq(value);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("returns PersonResponse when found")
        void returnsResponse() {
            var id = UUID.randomUUID();
            var person = testPerson(id);
            when(repository.findById(id)).thenReturn(Optional.of(person));

            PersonResponse response = personService.findById(id);

            assertThat(response.id()).isEqualTo(id);
            assertThat(response.firstName()).isEqualTo("Alex");
        }

        @Test
        @DisplayName("throws UserNotFoundException when not found")
        void throws_whenNotFound() {
            var id = UUID.randomUUID();
            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> personService.findById(id))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("updates person fields and returns response")
        void updatesFields() {
            var id = UUID.randomUUID();
            var person = testPerson(id);
            var request = new PersonRequest();
            request.setFirstName("Updated");
            request.setLastName("Name");
            request.setPhoneNumber("+9876543210");

            when(repository.existsById(id)).thenReturn(true);
            when(repository.getReferenceById(id)).thenReturn(person);
            when(repository.save(any(Person.class))).thenAnswer(inv -> inv.getArgument(0));

            PersonResponse response = personService.update(id, request);

            assertThat(response.firstName()).isEqualTo("Updated");
            assertThat(response.lastName()).isEqualTo("Name");
        }

        @Test
        @DisplayName("throws UserNotFoundException when person doesn't exist")
        void throws_whenNotFound() {
            var id = UUID.randomUUID();
            when(repository.existsById(id)).thenReturn(false);

            assertThatThrownBy(() -> personService.update(id, new PersonRequest()))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("existsById / getReferenceByIdIfExists")
    class Lookups {

        @Test
        @DisplayName("existsById delegates to repository")
        void existsById() {
            var id = UUID.randomUUID();
            when(repository.existsById(id)).thenReturn(true);
            assertThat(personService.existsById(id)).isTrue();
        }

        @Test
        @DisplayName("getReferenceByIdIfExists throws when not found")
        void getReference_throws() {
            var id = UUID.randomUUID();
            when(repository.existsById(id)).thenReturn(false);

            assertThatThrownBy(() -> personService.getReferenceByIdIfExists(id))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }
}
