package tech.github.oldlabclient.configuration;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.S3Configuration;

@Configuration
public class MinioConfig {

    @Value("${minio.bucket.default}")
    private String bucketDefault;

    @Value("${minio.bucket.persons}")
    private String bucketPersons;

    @Value("${minio.bucket.products}")
    private String bucketProducts;

    @Bean
    S3Client s3(@Value("${minio.url}") String url,
                @Value("${minio.access-key}") String ak,
                @Value("${minio.secret-key}") String sk) {

        S3Configuration s3cfg = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();

        return S3Client.builder()
                .endpointOverride(URI.create(url))
                .region(Region.EU_CENTRAL_1)
                .serviceConfiguration(s3cfg)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(ak, sk)))
                .build();
    }

    @Bean
    ApplicationRunner ensureBuckets(S3Client s3) {
        return args -> {
            ensureBucketExists(s3, bucketDefault);
            ensureBucketExists(s3, bucketPersons);
            ensureBucketExists(s3, bucketProducts);
        };
    }

    private void ensureBucketExists(S3Client s3, String bucket) {
        try {
            s3.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (S3Exception e) {
            if (e.statusCode() == 404) { // Not found -> create
                s3.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            } else {
                throw e;
            }
        }
    }
}
