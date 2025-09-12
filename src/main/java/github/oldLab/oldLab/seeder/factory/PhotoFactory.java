package github.oldLab.oldLab.seeder.factory;

import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import javax.imageio.ImageIO;

import com.github.javafaker.Faker;

import github.oldLab.oldLab.entity.Person;
import github.oldLab.oldLab.entity.Photo;
import github.oldLab.oldLab.entity.Shop;
import lombok.RequiredArgsConstructor;
import github.oldLab.oldLab.service.ImageProcessingService;
import github.oldLab.oldLab.service.PhotoStorage;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PhotoFactory implements DataFactory<Photo> {

    private final Faker faker;
    private final ImageProcessingService imageProcessingService;
    private final PhotoStorage photoStorage;

    private static final int WIDTH = 320;
    private static final int HEIGHT = 320;

    public Photo create(Person person) {
        byte[] processed = generateAndProcess();
        String key = photoStorage.save(processed, "image/webp");
        return Photo.builder()
                .objectKey(key)
                .contentType("image/webp")
                .size((long) processed.length)
                .person(person)
                .build();
    }

    public Photo create(Shop shop) {
        byte[] processed = generateAndProcess();
        String key = photoStorage.save(processed, "image/webp");
        return Photo.builder()
                .objectKey(key)
                .contentType("image/webp")
                .size((long) processed.length)
                .shop(shop)
                .build();
    }

    @Override
    public Photo create() {
        byte[] processed = generateAndProcess();
        String key = photoStorage.save(processed, "image/webp");
        return Photo.builder()
                .objectKey(key)
                .contentType("image/webp")
                .size((long) processed.length)
                .build();
    }

    private byte[] generateAndProcess() {
        try {
            byte[] png = generateRandomPng();
            return imageProcessingService.processImage(png, "image/png");
        } catch (IOException e) {
            log.warn("Failed to process generated image, using raw PNG: {}", e.getMessage());
            try {
                return generateRandomPng();
            } catch (IOException ex) {
                throw new RuntimeException("Failed to generate fallback image", ex);
            }
        }
    }

    private byte[] generateRandomPng() throws IOException {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        try {
            int r = ThreadLocalRandom.current().nextInt(256);
            int gr = ThreadLocalRandom.current().nextInt(256);
            int b = ThreadLocalRandom.current().nextInt(256);
            Color base = new Color(r, gr, b);
            g.setPaint(base);
            g.fillRect(0, 0, WIDTH, HEIGHT);

            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 28));
            String text = faker.lorem().characters(3).toUpperCase();
            int textWidth = g.getFontMetrics().stringWidth(text);
            g.drawString(text, (WIDTH - textWidth)/2, HEIGHT/2);

            // simple diagonal lines overlay
            g.setColor(new Color(255 - r, 255 - gr, 255 - b, 80));
            for (int i = -WIDTH; i < WIDTH; i+=40) {
                g.drawLine(i, 0, i + WIDTH, HEIGHT);
            }
        } finally {
            g.dispose();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }
}
