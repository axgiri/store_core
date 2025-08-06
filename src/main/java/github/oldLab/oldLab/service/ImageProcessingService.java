package github.oldLab.oldLab.service;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface ImageProcessingService {
    
    byte[] processImage(MultipartFile originalImage) throws IOException;
    
    byte[] processImage(byte[] imageData, String originalContentType) throws IOException;
}
