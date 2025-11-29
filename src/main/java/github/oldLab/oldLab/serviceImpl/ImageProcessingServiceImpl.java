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
            
            // Compress and convert to WebP directly from BufferedImage
            return compressAndConvertToWebp(image);

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
            
            return compressAndConvertToWebp(image);
        } catch (Exception e) {
            log.error("Error processing image: {}", e.getMessage(), e);
            throw new IOException("Failed to process image", e);
        }
    }

    /**
     * Compress and convert image to WebP format with adaptive quality
     */
    private byte[] compressAndConvertToWebp(BufferedImage image) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        float quality = INITIAL_QUALITY;
        byte[] result;

        // Try different quality levels until file size is acceptable
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

        // If still too large, reduce dimensions
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

        log.debug("Image compressed: final size {}KB, dimensions {}x{}", 
                result.length / 1024, image.getWidth(), image.getHeight());

        return result;
    }
}
