package github.oldLab.oldLab.serviceImpl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import github.oldLab.oldLab.service.PhotoStorage;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class MinioPhotoStorage implements PhotoStorage {

    private final S3Client s3;
    @Value("${minio.bucket}") private String bucket;

    @Override
    public String save(byte[] bytes, String contentType) {
        String key = UUID.randomUUID().toString();

        s3.putObject(
        PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)
            .build(),
            RequestBody.fromBytes(bytes));
            return key;
    }

    @Override
    public byte[] load(String objectKey) {
        return s3.getObjectAsBytes(GetObjectRequest
                .builder()
                .bucket(bucket)
                .key(objectKey)
                .build()).asByteArray();
    }

    @Override
    public void delete(String objectKey) {
        s3.deleteObject(DeleteObjectRequest.
        builder()
        .bucket(bucket)
        .key(objectKey)
        .build());
    }
}