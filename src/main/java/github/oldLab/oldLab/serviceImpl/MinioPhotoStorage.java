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
    
    @Value("${minio.bucket.default}")
    private String bucketDefault;

    @Value("${minio.bucket.persons}")
    private String bucketPersons;

    @Value("${minio.bucket.shops}")
    private String bucketShops;

    @Value("${minio.bucket.products}")
    private String bucketProducts;

    @Override
    public String saveDefault(byte[] bytes, String contentType) {
        String key = generateKey();   

        s3.putObject(
        PutObjectRequest.builder()
            .bucket(bucketDefault)
            .key(key)
            .contentType(contentType)
            .build(),
            RequestBody.fromBytes(bytes));
            return key;
    }

    @Override
    public byte[] loadDefault(String objectKey) {
        return s3.getObjectAsBytes(GetObjectRequest
                .builder()
                .bucket(bucketDefault)
                .key(objectKey)
                .build()).asByteArray();
    }

    @Override
    public String savePerson(byte[] bytes, String contentType) {
        String key = generateKey();   

        s3.putObject(
        PutObjectRequest.builder()
            .bucket(bucketPersons)
            .key(key)
            .contentType(contentType)
            .build(),
            RequestBody.fromBytes(bytes));
            return key;
    }

    @Override
    public byte[] loadPerson(String objectKey) {
        return s3.getObjectAsBytes(GetObjectRequest
                .builder()
                .bucket(bucketPersons)
                .key(objectKey)
                .build()).asByteArray();
    }

    @Override
    public String saveShop(byte[] bytes, String contentType) {
        String key = generateKey();   

        s3.putObject(
        PutObjectRequest.builder()
            .bucket(bucketShops)
            .key(key)
            .contentType(contentType)
            .build(),
            RequestBody.fromBytes(bytes));
            return key;
    }

    @Override
    public byte[] loadShop(String objectKey) {
        return s3.getObjectAsBytes(GetObjectRequest
                .builder()
                .bucket(bucketShops)
                .key(objectKey)
                .build()).asByteArray();
    }

    @Override
    public String saveProduct(byte[] bytes, String contentType) {
        String key = generateKey();   

        s3.putObject(
        PutObjectRequest.builder()
            .bucket(bucketProducts)
            .key(key)
            .contentType(contentType)
            .build(),
            RequestBody.fromBytes(bytes));
            return key;
    }

    @Override
    public byte[] loadProduct(String objectKey) {
        return s3.getObjectAsBytes(GetObjectRequest
                .builder()
                .bucket(bucketProducts)
                .key(objectKey)
                .build()).asByteArray();
    }

    @Override
    public void delete(String objectKey, String bucket) {
        s3.deleteObject(DeleteObjectRequest
            .builder()
            .bucket(bucket)
            .key(objectKey)
            .build());
    }

    private String generateKey() {
        return UUID.randomUUID().toString();
    }
}