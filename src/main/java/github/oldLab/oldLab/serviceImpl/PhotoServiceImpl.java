package github.oldLab.oldLab.serviceImpl;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import github.oldLab.oldLab.entity.Person;
import github.oldLab.oldLab.entity.Photo;
import github.oldLab.oldLab.entity.Shop;
import github.oldLab.oldLab.repository.PhotoRepository;
import github.oldLab.oldLab.service.PhotoService;
import github.oldLab.oldLab.service.PhotoStorage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PhotoServiceImpl implements PhotoService {

    private final PhotoRepository repository;
    private final PersonServiceImpl personService;
    private final ShopServiceImpl shopService;
    private final PhotoStorage storage;

    @Transactional
    public void uploadForPerson(Long personId, MultipartFile file) throws IOException {
        Person person = personService.findEntityById(personId);

        repository.findByPersonId(personId).ifPresent(this::removePhoto);

        String key = storage.save(file.getBytes(),file.getOriginalFilename(),detectContentType(file));

        Photo photo = Photo.builder()
                        .objectKey(key)
                        .contentType(file.getContentType())
                        .size(file.getSize())
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

        String key = storage.save(file.getBytes(),file.getOriginalFilename(),detectContentType(file));

        Photo ph = Photo.builder()
                        .objectKey(key)
                        .contentType(file.getContentType())
                        .size(file.getSize())
                        .shop(shop)
                        .build();
        repository.save(ph);
    }

    public byte[] loadForShop(Long shopId) {
        Photo ph = repository.findByShopId(shopId)
                         .orElseThrow(() -> new RuntimeException("Photo not set"));

        return storage.load(ph.getObjectKey());
    }

    private void removePhoto(Photo ph) {
        storage.delete(ph.getObjectKey());
        repository.delete(ph);
    }

    private String detectContentType(MultipartFile f) {
        return (f.getContentType() != null) ? f.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}

