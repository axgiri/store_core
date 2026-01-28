package tech.github.oldlabclient.service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import tech.github.oldlabclient.dto.response.ProductPhotoResponse;
import tech.github.oldlabclient.entity.Person;
import tech.github.oldlabclient.entity.Photo;
import tech.github.oldlabclient.entity.Product;
import tech.github.oldlabclient.exception.PhotoNotFoundException;
import tech.github.oldlabclient.exception.ProductNotFoundException;
import tech.github.oldlabclient.repository.PhotoRepository;
import tech.github.oldlabclient.service.PhotoService;

@Service
@RequiredArgsConstructor
public class PhotoService{

    private final PhotoRepository repository;
    private final PersonService personService;
    private final ProductService productService;
    private final MinioPhotoStorage storage;
    private final ImageProcessingService imageProcessor;

    @Value("${max.photo.per.product}")
    private int maxPhotosPerProduct;

    @Value("${minio.bucket.persons}")
    private String bucketPersons;

    @Value("${minio.bucket.products}")
    private String bucketProducts;

    @Transactional
    public void uploadForPerson(UUID personId, MultipartFile file) throws IOException {
        Person person = personService.getReferenceByIdIfExists(personId);

        repository.findByPersonId(personId).ifPresent(this::removePhoto);

        byte[] processedImage = imageProcessor.processImage(file);

        String key = storage.save(processedImage, "image/webp", bucketPersons);

        Photo photo = Photo.builder()
                        .objectKey(key)
                        .contentType("image/webp")
                        .size((long) processedImage.length)
                        .person(person)
                        .bucket(bucketPersons)
                        .createdAt(Instant.now())
                        .build();
        repository.save(photo);
    }

    public byte[] loadForPerson(UUID personId) {
        Photo photo = repository.findByPersonId(personId)
                         .orElseThrow(() -> new PhotoNotFoundException("avatar not set"));
        return storage.load(photo.getObjectKey(), bucketPersons);
    }

    @Transactional
    public void deleteForPerson(UUID personId) {
        repository.findByPersonId(personId).ifPresent(this::removePhoto);
    }

    @Transactional
    public void uploadForProduct(Long productId, MultipartFile file) throws IOException {

        var stats = repository.findProductExistsAndPhotoCount(productId);

        if (stats == null || !stats.isExists()) {
            throw new ProductNotFoundException("product not found: " + productId);
        }

        
        if (stats.getCount() >= maxPhotosPerProduct) {
            throw new ProductNotFoundException("max photos per product reached");
        }

        Product product = productService.getReferenceByIdIfExists(productId);

        byte[] processedImage = imageProcessor.processImage(file);

        String key = storage.save(processedImage, "image/webp", bucketProducts);

        Photo photo = Photo.builder()
                        .objectKey(key)
                        .contentType("image/webp") 
                        .size((long) processedImage.length)
                        .product(product)
                        .bucket(bucketProducts)
                        .createdAt(Instant.now())
                        .build();
        repository.save(photo);
    }

    public List<ProductPhotoResponse> loadForProduct(Long productId) {
        productService.getReferenceByIdIfExists(productId);

        List<Photo> photos = repository.findAllByProductId(productId);
        
        return photos.stream()
                .map(photo -> {
                    ProductPhotoResponse dto = new ProductPhotoResponse();
                    dto.setObjectKey(photo.getObjectKey());
                    byte[] fileBytes = storage.load(photo.getObjectKey(), bucketProducts);
                    dto.setFile(fileBytes);
                    return dto;
                })
                .toList();
    }

    public void deleteForProduct(Long productId, String objectKey) {
        Photo photo = repository.findByProductIdAndObjectKey(productId, objectKey)
                .orElseThrow(() -> new PhotoNotFoundException("photo not found for product: " + productId + " and key: " + objectKey));
        removePhoto(photo);
    }


    public void removePhoto(Photo ph) {
        storage.delete(ph.getObjectKey(), ph.getBucket());
        repository.delete(ph);
        repository.flush();
    }
}

