package github.oldLab.oldLab.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import github.oldLab.oldLab.dto.response.ProductPhotoResponse;
import github.oldLab.oldLab.entity.Photo;

public interface PhotoService {
    void uploadForPerson(Long personId, MultipartFile file) throws IOException;

    byte[] loadForPerson(Long personId);

    void deleteForPerson(Long personId);

    void uploadForProduct(Long productId, MultipartFile file) throws IOException;

    List<ProductPhotoResponse> loadForProduct(Long productId);

    void deleteForProduct(Long productId, String objectKey);

    void removePhoto(Photo ph);
}
