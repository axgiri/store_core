package tech.github.storecore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import tech.github.storecore.entity.Person;
import tech.github.storecore.entity.Photo;
import tech.github.storecore.exception.MaxPhotosPerProductReachedException;
import tech.github.storecore.exception.PhotoNotFoundException;
import tech.github.storecore.exception.ProductNotFoundException;
import tech.github.storecore.repository.PhotoRepository;

@ExtendWith(MockitoExtension.class)
class PhotoServiceTest {

    @Mock private PhotoRepository repository;
    @Mock private PersonService personService;
    @Mock private MinioPhotoStorage storage;
    @Mock private ImageProcessingService imageProcessor;

    @InjectMocks
    private PhotoService photoService;

    private void setFields() {
        ReflectionTestUtils.setField(photoService, "maxPhotosPerProduct", 5);
        ReflectionTestUtils.setField(photoService, "bucketPersons", "persons");
        ReflectionTestUtils.setField(photoService, "bucketProducts", "products");
    }

    @Nested
    @DisplayName("uploadForPerson")
    class UploadForPerson {

        @Test
        @DisplayName("processes image, stores in minio, saves photo entity")
        void uploadsSuccessfully() throws IOException {
            setFields();
            var personId = UUID.randomUUID();
            var person = Person.builder().id(personId).firstName("A").lastName("B").build();
            var file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", new byte[]{1, 2, 3});
            byte[] processed = new byte[]{10, 20};

            when(personService.getReferenceByIdIfExists(personId)).thenReturn(person);
            when(repository.findByPersonId(personId)).thenReturn(Optional.empty());
            when(imageProcessor.processImage(file)).thenReturn(processed);
            when(storage.save(processed, "image/webp", "persons")).thenReturn("key-123");

            photoService.uploadForPerson(personId, file);

            verify(storage).save(processed, "image/webp", "persons");
            verify(repository).save(any(Photo.class));
        }

        @Test
        @DisplayName("removes existing avatar before uploading new one")
        void removesOldAvatar() throws IOException {
            setFields();
            var personId = UUID.randomUUID();
            var person = Person.builder().id(personId).firstName("A").lastName("B").build();
            var oldPhoto = Photo.builder()
                    .objectKey("old-key")
                    .bucket("persons")
                    .contentType("image/webp")
                    .size(100L)
                    .createdAt(Instant.now())
                    .build();
            var file = new MockMultipartFile("file", "new.jpg", "image/jpeg", new byte[]{1});

            when(personService.getReferenceByIdIfExists(personId)).thenReturn(person);
            when(repository.findByPersonId(personId)).thenReturn(Optional.of(oldPhoto));
            when(imageProcessor.processImage(file)).thenReturn(new byte[]{1});
            when(storage.save(any(), any(), any())).thenReturn("new-key");

            photoService.uploadForPerson(personId, file);

            verify(storage).delete("old-key", "persons");
            verify(repository).delete(oldPhoto);
        }
    }

    @Nested
    @DisplayName("loadForPerson")
    class LoadForPerson {

        @Test
        @DisplayName("returns bytes from storage")
        void loadsSuccessfully() {
            setFields();
            var personId = UUID.randomUUID();
            var photo = Photo.builder().objectKey("key-1").bucket("persons").build();
            when(repository.findByPersonId(personId)).thenReturn(Optional.of(photo));
            when(storage.load("key-1", "persons")).thenReturn(new byte[]{42});

            byte[] result = photoService.loadForPerson(personId);

            assertThat(result).containsExactly(42);
        }

        @Test
        @DisplayName("throws PhotoNotFoundException when no avatar")
        void throws_whenNoAvatar() {
            var personId = UUID.randomUUID();
            when(repository.findByPersonId(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> photoService.loadForPerson(personId))
                    .isInstanceOf(PhotoNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("uploadForProduct")
    class UploadForProduct {

        @Test
        @DisplayName("throws ProductNotFoundException when product missing")
        void throws_whenProductMissing() {
            setFields();
            when(repository.findProductExistsAndPhotoCount(99L)).thenReturn(null);

            var file = new MockMultipartFile("f", "img.jpg", "image/jpeg", new byte[]{1});

            assertThatThrownBy(() -> photoService.uploadForProduct(99L, file))
                    .isInstanceOf(ProductNotFoundException.class);
        }

        @Test
        @DisplayName("throws MaxPhotosPerProductReachedException when limit exceeded")
        void throws_whenMaxReached() {
            setFields();
            var projection = new ProductExistCountProjection() {
                @Override public boolean isExists() { return true; }
                @Override public long getCount() { return 5; }
            };
            when(repository.findProductExistsAndPhotoCount(1L)).thenReturn(projection);

            var file = new MockMultipartFile("f", "img.jpg", "image/jpeg", new byte[]{1});

            assertThatThrownBy(() -> photoService.uploadForProduct(1L, file))
                    .isInstanceOf(MaxPhotosPerProductReachedException.class);
        }
    }

    @Nested
    @DisplayName("deleteForProduct")
    class DeleteForProduct {

        @Test
        @DisplayName("deletes photo from storage and DB")
        void deletesPhoto() {
            var photo = Photo.builder()
                    .objectKey("key")
                    .bucket("products")
                    .contentType("image/webp")
                    .size(50L)
                    .createdAt(Instant.now())
                    .build();
            when(repository.findByProductIdAndObjectKey(1L, "key")).thenReturn(Optional.of(photo));

            photoService.deleteForProduct(1L, "key");

            verify(storage).delete("key", "products");
            verify(repository).delete(photo);
        }

        @Test
        @DisplayName("throws PhotoNotFoundException when photo missing")
        void throws_whenMissing() {
            when(repository.findByProductIdAndObjectKey(1L, "none")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> photoService.deleteForProduct(1L, "none"))
                    .isInstanceOf(PhotoNotFoundException.class);
        }
    }
}
