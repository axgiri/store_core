package github.oldLab.oldLab.service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.springframework.web.multipart.MultipartFile;

import github.oldLab.oldLab.dto.response.ProductPhotoResponse;
import github.oldLab.oldLab.entity.Photo;
import github.oldLab.oldLab.repository.PhotoRepository;

public interface PhotoService {
    default void removePhoto(Photo ph) {
        getStorage().delete(ph.getObjectKey());
        getRepository().delete(ph);
        getRepository().flush();
    }

    void uploadForPerson(Long personId, MultipartFile file) throws IOException;

    byte[] loadForPerson(Long personId);

    void deleteForPerson(Long personId);

    void uploadForShop(Long shopId, MultipartFile file) throws IOException;

    byte[] loadForShop(Long shopId);

    void deleteForShop(Long shopId);

    void uploadForProduct(Long productId, MultipartFile file) throws IOException;

    List<ProductPhotoResponse> loadForProduct(Long productId);

    void deleteForProduct(Long productId, String objectKey);

    PhotoStorage getStorage();
    PhotoRepository getRepository();
}
