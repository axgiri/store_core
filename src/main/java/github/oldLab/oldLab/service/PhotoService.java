package github.oldLab.oldLab.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface PhotoService {
    
    void uploadForPerson(Long personId, MultipartFile file) throws IOException;

    byte[] loadForPerson(Long personId);

    void deleteForPerson(Long personId);

    void uploadForShop(Long shopId, MultipartFile file) throws IOException;

    byte[] loadForShop(Long shopId);

    void deleteForShop(Long shopId);
}
