package github.oldLab.oldLab.serviceImpl;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import github.oldLab.oldLab.entity.Person;
import github.oldLab.oldLab.entity.Photo;
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
    private final PhotoStorage storage;
    private final ImageProcessingService imageProcessor;

    @Transactional
    public void uploadForPerson(Long personId, MultipartFile file) throws IOException {
        Person person = personService.findEntityById(personId);

        repository.findByPersonId(personId).ifPresent(this::removePhoto);

        byte[] processedImage = imageProcessor.processImage(file);

        String key = storage.save(processedImage, file.getOriginalFilename(), "image/webp");

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
    public void uploadForShop(Long shopId, MultipartFile file) throws IOException {
        Shop shop = shopService.findEntityById(shopId);

        repository.findByShopId(shopId).ifPresent(this::removePhoto);

        byte[] processedImage = imageProcessor.processImage(file);

        String key = storage.save(processedImage, file.getOriginalFilename(), "image/webp");

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

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    private void removePhoto(Photo ph) {
        storage.delete(ph.getObjectKey());
        repository.delete(ph);
        repository.flush();
    }
}

