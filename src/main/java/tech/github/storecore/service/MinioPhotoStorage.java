package tech.github.storecore.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioPhotoStorage {

    private final S3Client s3;

    public String save(byte[] bytes, String contentType, String bucket) {
        String key = generateKey();   

        s3.putObject(
        PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)
            .build(),
            RequestBody.fromBytes(bytes));
            return key;
    }

    public byte[] load(String objectKey, String bucket) {
        return s3.getObjectAsBytes(GetObjectRequest
            .builder()
            .bucket(bucket)
            .key(objectKey)
            .build()).asByteArray();
    }

    public void delete(String objectKey, String bucket) {
        s3.deleteObject(DeleteObjectRequest
            .builder()
            .bucket(bucket)
            .key(objectKey)
            .build());
    }

    public boolean safeDelete(String objectKey, String bucket) {
        try {
            delete(objectKey, bucket);
            return true;
        } catch (NoSuchKeyException e) {
            log.debug("Object '{}' already deleted or doesn't exist in bucket '{}'", objectKey, bucket);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete object '{}' from bucket '{}': {}", objectKey, bucket, e.getMessage(), e);
            return false;
        }
    }

    private String generateKey() {
        return UUID.randomUUID().toString();
    }
}