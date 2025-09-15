package github.oldLab.oldLab.serviceImpl;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import github.oldLab.oldLab.dto.response.ProductPhotoResponse;
import github.oldLab.oldLab.entity.Person;
import github.oldLab.oldLab.entity.Photo;
import github.oldLab.oldLab.entity.Product;
import github.oldLab.oldLab.entity.Shop;
import github.oldLab.oldLab.repository.PhotoRepository;
import github.oldLab.oldLab.service.ImageProcessingService;
import github.oldLab.oldLab.service.PhotoService;
import github.oldLab.oldLab.service.PhotoStorage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PhotoServiceImpl implements PhotoService {

    private final PhotoRepository repository;
    private final PersonServiceImpl personService;
    private final ShopServiceImpl shopService;
    private final ProductServiceImpl productService;
    private final PhotoStorage storage;
    private final ImageProcessingService imageProcessor;

    @Value("${max.photo.per.product}")
    private int maxPhotosPerProduct;

    @Transactional
    public void uploadForPerson(Long personId, MultipartFile file) throws IOException {
        Person person = personService.findEntityById(personId);

        repository.findByPersonId(personId).ifPresent(this::removePhoto);

        byte[] processedImage = imageProcessor.processImage(file);

        String key = storage.save(processedImage, "image/webp");

        Photo photo = Photo.builder()
                        .objectKey(key)
                        .contentType("image/webp")
                        .size((long) processedImage.length)
                        .person(person)
                        .build();
        repository.save(photo);
    }

    public byte[] loadForPerson(Long personId) {
        Photo photo = repository.findByPersonId(personId)
                         .orElseThrow(() -> new RuntimeException("avatar not set"));
        return storage.load(photo.getObjectKey());
    }

    @Transactional
    public void deleteForPerson(Long personId) {
        repository.findByPersonId(personId).ifPresent(this::removePhoto);
    }

    @Transactional
    public void uploadForShop(Long shopId, MultipartFile file) throws IOException {
        Shop shop = shopService.findEntityById(shopId);

        repository.findByShopId(shopId).ifPresent(this::removePhoto);

        byte[] processedImage = imageProcessor.processImage(file);

        String key = storage.save(processedImage, "image/webp");

        Photo photo = Photo.builder()
                        .objectKey(key)
                        .contentType("image/webp") 
                        .size((long) processedImage.length)
                        .shop(shop)
                        .build();
        repository.save(photo);
    }

    public byte[] loadForShop(Long shopId) {
        Photo ph = repository.findByShopId(shopId)
                         .orElseThrow(() -> new RuntimeException("Photo not set"));

        return storage.load(ph.getObjectKey());
    }

    @Transactional
    public void deleteForShop(Long shopId) {
        repository.findByShopId(shopId).ifPresent(this::removePhoto);
    }

    @Transactional
    public void uploadForProduct(Long productId, MultipartFile file) throws IOException {

        var stats = repository.findProductExistsAndPhotoCount(productId);

        if (stats == null || !stats.isExists()) {
            throw new RuntimeException("product not found: " + productId);
        }

        
        if (stats.getCount() >= maxPhotosPerProduct) {
            throw new RuntimeException("max photos per product reached");
        }

        Product product = productService.getReferenceIfExists(productId);

        byte[] processedImage = imageProcessor.processImage(file);

        String key = storage.save(processedImage, "image/webp");

        Photo photo = Photo.builder()
                        .objectKey(key)
                        .contentType("image/webp") 
                        .size((long) processedImage.length)
                        .product(product)
                        .build();
        repository.save(photo);
    }

    @Override
    public List<ProductPhotoResponse> loadForProduct(Long productId) {
        productService.getReferenceIfExists(productId);

        List<Photo> photos = repository.findAllByProductId(productId);
        
        return photos.stream()
                .map(photo -> {
                    ProductPhotoResponse dto = new ProductPhotoResponse();
                    dto.setObjectKey(photo.getObjectKey());
                    byte[] fileBytes = storage.load(photo.getObjectKey());
                    dto.setFile(fileBytes);
                    return dto;
                })
                .toList();
    }

    @Override
    public void deleteForProduct(Long productId, String objectKey) {
        List<Photo> photos = repository.findAllByProductId(productId);
        photos.stream()
                .filter(photo -> photo.getObjectKey().equals(objectKey))
                .findFirst()
                .ifPresent(this::removePhoto);
    }

    @Override
    public PhotoStorage getStorage() {
        return storage;
    }

    @Override
    public PhotoRepository getRepository() {
        return repository;
    }
}

