package tech.github.storecore.service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Object;
import tech.github.storecore.entity.Photo;
import tech.github.storecore.repository.PhotoRepository;
import tech.github.storecore.repository.ProductRepository;
import tech.github.storecore.search.ProductDocument;
import tech.github.storecore.search.ProductSearchRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledCleanupService {

    private final S3Client s3Client;
    private final PhotoRepository photoRepository;
    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;

    @Value("${minio.bucket.default}")
    private String bucketDefault;

    @Value("${minio.bucket.persons}")
    private String bucketPersons;

    @Value("${minio.bucket.products}")
    private String bucketProducts;

    @Scheduled(cron = "0 0 0 * * ?", zone = "${app.cleanup.timezone:CET}")
    @Transactional(readOnly = true)
    public void cleanupOrphanedStorageFiles() {
        log.info("Starting MinIO orphaned files cleanup...");
        
        AtomicInteger totalDeleted = new AtomicInteger(0);
        
        totalDeleted.addAndGet(cleanupBucket(bucketDefault));
        totalDeleted.addAndGet(cleanupBucket(bucketPersons));
        totalDeleted.addAndGet(cleanupBucket(bucketProducts));
        
        log.info("MinIO cleanup completed: {} orphaned files deleted", totalDeleted.get());
    }

    private int cleanupBucket(String bucket) {
        log.debug("Cleaning up bucket: {}", bucket);
        
        int deletedCount = 0;
        String continuationToken = null;
        
        Set<String> dbObjectKeys = new HashSet<>(photoRepository.findAll().stream()
                .filter(photo -> bucket.equals(photo.getBucket()))
                .map(Photo::getObjectKey)
                .toList());
        
        log.debug("Found {} files in DB for bucket '{}'", dbObjectKeys.size(), bucket);
        
        try {
            do {
                ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                        .bucket(bucket)
                        .maxKeys(1000);
                
                if (continuationToken != null) {
                    requestBuilder.continuationToken(continuationToken);
                }
                
                ListObjectsV2Response response = s3Client.listObjectsV2(requestBuilder.build());
                for (S3Object s3Object : response.contents()) {
                    String objectKey = s3Object.key();
                    
                    if (!dbObjectKeys.contains(objectKey)) {
                        boolean deleted = safeDeleteFromStorage(objectKey, bucket);
                        if (deleted) {
                            deletedCount++;
                            log.debug("Deleted orphaned file: bucket='{}', key='{}'", bucket, objectKey);
                        }
                    }
                }
                continuationToken = Boolean.TRUE.equals(response.isTruncated()) ? response.nextContinuationToken() : null;
            } while (continuationToken != null);
            
        } catch (Exception e) {
            log.error("Error cleaning up bucket '{}': {}", bucket, e.getMessage(), e);
        }
        
        log.debug("Deleted {} orphaned files from bucket '{}'", deletedCount, bucket);
        return deletedCount;
    }

    private boolean safeDeleteFromStorage(String objectKey, String bucket) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            log.debug("Object '{}' already deleted from bucket '{}'", objectKey, bucket, e);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete object '{}' from bucket '{}': {}", objectKey, bucket, e.getMessage(), e);
            return false;
        }
    }

    
    @Scheduled(cron = "0 0 4 * * ?", zone = "${app.cleanup.timezone:CET}")
    @Transactional(readOnly = true)
    public void reindexDeletedProducts() {
        log.info("Starting Elasticsearch orphaned documents cleanup...");
        
        int deletedCount = 0;
        int pageNumber = 0;
        int pageSize = 1000;
        
        try {
            Page<ProductDocument> page;
            do {
                page = productSearchRepository.findAll(PageRequest.of(pageNumber, pageSize));
                for (ProductDocument document : page.getContent()) {
                    Long productId = document.getId();

                    if (!productRepository.existsById(productId)) {
                        deletedCount = getDeletedCount(productId, deletedCount);
                    }
                }
                
                pageNumber++;
                
            } while (page.hasNext());
            
            log.info("Elasticsearch cleanup completed: {} orphaned documents deleted", deletedCount);
            
        } catch (Exception e) {
            log.error("Error during Elasticsearch cleanup: {}", e.getMessage(), e);
        }
    }

    private int getDeletedCount(Long productId, int deletedCount) {
        try {
            productSearchRepository.deleteById(productId);
            deletedCount++;
            log.debug("Deleted orphaned Elasticsearch document: productId={}", productId);
        } catch (Exception e) {
            log.warn("Failed to delete Elasticsearch document for product {}: {}",
                    productId, e.getMessage());
        }
        return deletedCount;
    }
}
