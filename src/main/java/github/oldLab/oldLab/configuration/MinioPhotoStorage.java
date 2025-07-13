package github.oldLab.oldLab.configuration;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
class MinioConfig {

    @Bean
    S3Client s3(@Value("${minio.url}") String url,
                @Value("${minio.access-key}") String ak,
                @Value("${minio.secret-key}") String sk) {

        return S3Client.builder()
                .endpointOverride(URI.create(url))
                .region(Region.EU_CENTRAL_1)
                .credentialsProvider(
                StaticCredentialsProvider.create(AwsBasicCredentials.create(ak, sk)))
                .build();
    }
}
