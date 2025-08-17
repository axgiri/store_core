package github.oldLab.oldLab.serviceImpl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import github.oldLab.oldLab.service.ImageProcessingService;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

@Service
@Slf4j
public class ImageProcessingServiceImpl implements ImageProcessingService {

    private static final long MAX_FILE_SIZE = 200 * 1024;

    private static final float INITIAL_QUALITY = 0.85f;

    private static final float MIN_QUALITY = 0.5f;

    private static final float QUALITY_DECREMENT = 0.05f;

    private static final String OUTPUT_FORMAT = "webp";

    @Override
    public byte[] processImage(MultipartFile originalImage) throws IOException {
        try {
            BufferedImage image = ImageIO.read(originalImage.getInputStream());
            
            if (image == null) {
                throw new IOException("Couldn't read image: " + originalImage.getOriginalFilename());
            }
            
            return convertToWebpAndCompress(image);
        } catch (Exception e) {
            log.error("Error processing image: {}", e.getMessage(), e);
            throw new IOException("Failed to process image", e);
        }
    }

    @Override
    public byte[] processImage(byte[] imageData, String originalContentType) throws IOException {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            
            if (image == null) {
                throw new IOException("Couldn't read image from byte array");
            }
            
            return convertToWebpAndCompress(image);
        } catch (Exception e) {
            log.error("Error processing image: {}", e.getMessage(), e);
            throw new IOException("Failed to process image", e);
        }
    }
    
    private byte[] convertToWebpAndCompress(BufferedImage image) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        float quality = INITIAL_QUALITY;
        byte[] result;

        do {
            outputStream.reset();

            Thumbnails.of(image)
                .size(image.getWidth(), image.getHeight())
                .outputQuality(quality)
                .outputFormat(OUTPUT_FORMAT)
                .toOutputStream(outputStream);

            result = outputStream.toByteArray();

            quality -= QUALITY_DECREMENT;
            
        } while (result.length > MAX_FILE_SIZE && quality >= MIN_QUALITY);

        if (result.length > MAX_FILE_SIZE) {
            log.info("Image still too large ({}KB) at minimum quality, reducing dimensions", 
                    result.length / 1024);

            double scaleFactor = Math.sqrt(MAX_FILE_SIZE / (double) result.length);

            outputStream.reset();
            Thumbnails.of(image)
                .scale(scaleFactor)
                .outputQuality(MIN_QUALITY)
                .outputFormat(OUTPUT_FORMAT)
                .toOutputStream(outputStream);
            
            result = outputStream.toByteArray();
        }

        log.info("Image processed: original size {}x{}, final size: {}KB", 
                image.getWidth(), image.getHeight(), result.length / 1024);

        return result;
    }
}
