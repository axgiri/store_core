package tech.github.storecore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import tech.github.storecore.dto.request.ProductRequest;
import tech.github.storecore.dto.response.ProductResponse;
import tech.github.storecore.entity.Person;
import tech.github.storecore.entity.Product;
import tech.github.storecore.enumeration.CategoryEnum;
import tech.github.storecore.exception.ProductNotFoundException;
import tech.github.storecore.exception.UserNotFoundException;
import tech.github.storecore.repository.ProductRepository;
import tech.github.storecore.search.ProductDocument;
import tech.github.storecore.search.ProductSearchRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository repository;
    @Mock private PersonService personService;
    @Mock private ProductSearchRepository productSearchRepository;

    @InjectMocks
    private ProductService productService;

    private Person testPerson() {
        return Person.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    private Product testProduct(Person person) {
        return new Product()
                .setId(1L)
                .setName("Laptop")
                .setDescription("Gaming laptop")
                .setPrice(BigDecimal.valueOf(1500))
                .setCategory(CategoryEnum.LAPTOPS)
                .setIsAvailable(true)
                .setPerson(person);
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("saves product and indexes in ES, returns response")
        void createsProduct() {
            var person = testPerson();
            var request = new ProductRequest();
            request.setName("Laptop");
            request.setDescription("Gaming");
            request.setPrice(BigDecimal.valueOf(1500));
            request.setCategory(CategoryEnum.LAPTOPS);

            var savedProduct = testProduct(person);
            when(personService.getReferenceById(person.getId())).thenReturn(person);
            when(repository.save(any(Product.class))).thenReturn(savedProduct);

            ProductResponse response = productService.create(request, person.getId());

            assertThat(response.name()).isEqualTo("Laptop");
            assertThat(response.category()).isEqualTo(CategoryEnum.LAPTOPS);
            verify(productSearchRepository).save(any(ProductDocument.class));
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("returns product response when found")
        void returnsProduct() {
            var person = testPerson();
            var product = testProduct(person);
            when(repository.findById(1L)).thenReturn(Optional.of(product));

            ProductResponse response = productService.getById(1L);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.name()).isEqualTo("Laptop");
        }

        @Test
        @DisplayName("throws ProductNotFoundException when not found")
        void throws_whenNotFound() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getById(999L))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("listByPersonId")
    class ListByPersonId {

        @Test
        @DisplayName("returns products for existing person")
        void returnsList() {
            var personId = UUID.randomUUID();
            var person = Person.builder().id(personId).firstName("A").lastName("B").build();
            var product = testProduct(person);
            var page = new PageImpl<>(List.of(product));

            when(personService.existsById(personId)).thenReturn(true);
            when(repository.findByPersonId(eq(personId), any(PageRequest.class))).thenReturn(page);

            var result = productService.listByPersonId(personId, 0, 10);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().name()).isEqualTo("Laptop");
        }

        @Test
        @DisplayName("throws UserNotFoundException when person not found")
        void throws_whenPersonMissing() {
            var id = UUID.randomUUID();
            when(personService.existsById(id)).thenReturn(false);

            assertThatThrownBy(() -> productService.listByPersonId(id, 0, 10))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("updates product fields and re-indexes")
        void updatesProduct() {
            var person = testPerson();
            var existing = testProduct(person);
            var request = new ProductRequest();
            request.setName("Updated");
            request.setDescription("Updated desc");
            request.setPrice(BigDecimal.valueOf(2000));
            request.setCategory(CategoryEnum.PC);

            when(repository.findById(1L)).thenReturn(Optional.of(existing));
            when(repository.save(any(Product.class))).thenReturn(existing);

            ProductResponse response = productService.update(1L, request);

            assertThat(response).isNotNull();
            verify(productSearchRepository).save(any(ProductDocument.class));
        }

        @Test
        @DisplayName("throws when product does not exist")
        void throws_whenNotFound() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.update(999L, new ProductRequest()))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deletes from DB and ES")
        void deletesProduct() {
            var person = testPerson();
            var product = testProduct(person);
            when(repository.findById(1L)).thenReturn(Optional.of(product));

            productService.delete(1L);

            verify(repository).delete(product);
            verify(productSearchRepository).deleteById(1L);
        }

        @Test
        @DisplayName("throws when product not found")
        void throws_whenNotFound() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.delete(999L))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findEntityById / getReferenceByIdIfExists")
    class EntityLookups {

        @Test
        @DisplayName("findEntityById throws when missing")
        void findEntity_throws() {
            when(repository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.findEntityById(1L))
                    .isInstanceOf(ProductNotFoundException.class);
        }

        @Test
        @DisplayName("getReferenceByIdIfExists throws when not exists")
        void getReference_throws() {
            when(repository.existsById(1L)).thenReturn(false);

            assertThatThrownBy(() -> productService.getReferenceByIdIfExists(1L))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }
}
