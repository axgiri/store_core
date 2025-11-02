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
            
            if (!isWebP(originalImage.getOriginalFilename())) {
                byte[] webpImage = convertToWebp(image);
                return compressImage(webpImage);
            } else {
                return compressImage(originalImage.getBytes());
            }

        } catch (OutOfMemoryError e) {
            log.error("Out of memory while processing image: {}", e.getMessage());
            throw new IOException("Image processing failed: out of memory", e);
        } catch (StackOverflowError e) {
            log.error("Stack overflow while processing image: {}", e.getMessage());
            throw new IOException("Image processing failed: stack overflow", e);
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
            
            byte[] webpImage = convertToWebp(image);
            return compressImage(webpImage);
        } catch (Exception e) {
            log.error("Error processing image: {}", e.getMessage(), e);
            throw new IOException("Failed to process image", e);
        }
    }

    private byte[] convertToWebp(BufferedImage image) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.of(image)
            .size(image.getWidth(), image.getHeight())
            .outputQuality(INITIAL_QUALITY)
            .outputFormat(OUTPUT_FORMAT)
            .toOutputStream(outputStream);

        log.debug("Image converted to WebP: original size {}x{}", image.getWidth(), image.getHeight());
        return outputStream.toByteArray();
    }

    private byte[] compressImage(byte[] webpImage) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(webpImage));
        
        if (image == null) {
            return webpImage;
        }

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
            log.debug("Image still too large ({}KB) at minimum quality, reducing dimensions",
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

        log.debug("Image compressed: final size {}KB", result.length / 1024);

        return result;
    }

    private boolean isWebP(String filename) {
        if (filename == null)
            return false;

        return filename.toLowerCase().endsWith(".webp");
    }
}
