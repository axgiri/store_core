package github.oldLab.oldLab.controller;

import java.util.List;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import github.oldLab.oldLab.dto.response.ProductPhotoResponse;
import github.oldLab.oldLab.service.PhotoService;
import github.oldLab.oldLab.serviceImpl.RateLimiterServiceImpl;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/photos")
@RequiredArgsConstructor
@Slf4j
public class PhotoController {

    private final PhotoService service;
    private final RateLimiterServiceImpl rateLimiterService;

    @PutMapping(path = "/persons/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@accessControlService.isSelf(authentication, #id) or @accessControlService.isAdmin(authentication)")
    public ResponseEntity<Void> uploadPersonPhoto(@PathVariable Long id,
                                  @RequestPart("file") MultipartFile file,
                                  HttpServletRequest httpRequest) throws Exception {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("upload person photo id: {}", id);
            service.uploadForPerson(id, file);
            return ResponseEntity.ok().build();
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @GetMapping("/persons/{id}")
    public ResponseEntity<byte[]> getPersonPhoto(@PathVariable Long id, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            byte[] bytes = service.loadForPerson(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("image/webp"))
                    .cacheControl(CacheControl.noCache())
                    .body(bytes);
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @PutMapping(path = "/shops/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@accessControlService.isCompanyWorker(authentication, #id) or @accessControlService.isAdmin(authentication)")
    public ResponseEntity<Void> uploadShopPhoto(@PathVariable Long id,
                                @RequestPart("file") MultipartFile file,
                                HttpServletRequest httpRequest) throws Exception {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("upload shop photo id: {}", id);
            service.uploadForShop(id, file);
            return ResponseEntity.ok().build();
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @GetMapping("/shops/{id}")
    public ResponseEntity<byte[]> getShopPhoto(@PathVariable Long id, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            byte[] bytes = service.loadForShop(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("image/webp"))
                    .cacheControl(CacheControl.noCache())
                    .body(bytes);
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<List<ProductPhotoResponse>> getProductPhoto(@PathVariable Long id, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            List<ProductPhotoResponse> response = service.loadForProduct(id);
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noCache())
                    .body(response);
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }
    
    @PostMapping(path = "/products/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@accessControlService.isCompanyWorker(authentication, #id) or @accessControlService.isAdmin(authentication)")
    public ResponseEntity<Void> uploadProductPhotos(@PathVariable Long id,
                                   @RequestPart("file") MultipartFile file,
                                   HttpServletRequest httpRequest) throws Exception {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("upload product photos id: {}", id);
            service.uploadForProduct(id, file);
            return ResponseEntity.ok().build();
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }
    
    
    @DeleteMapping("/products/{id}/{objectKey}") // проверка с photoRepository.existsByIdAndProductId(photoId, id)
    @PreAuthorize("@accessControlService.isCompanyWorker(authentication, #id) or @accessControlService.isAdmin(authentication)")
    public ResponseEntity<Void> deleteProductPhoto(@PathVariable Long id,
                                   @PathVariable String objectKey,
                                   HttpServletRequest httpRequest) throws Exception {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("delete product photo id: {}, objectKey: {}", id, objectKey);
            service.deleteForProduct(id, objectKey);
            return ResponseEntity.noContent().build();
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @DeleteMapping("/persons/{id}")
    @PreAuthorize("@accessControlService.isSelf(authentication, #id) or @accessControlService.isAdmin(authentication)")
    public ResponseEntity<Void> deletePersonPhoto(@PathVariable Long id,
                                HttpServletRequest httpRequest) throws Exception {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("delete person photo id: {}", id);
            service.deleteForPerson(id);
            return ResponseEntity.noContent().build();
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @DeleteMapping("/shops/{id}")
    @PreAuthorize("@accessControlService.isCompanyWorker(authentication, #id) or @accessControlService.isAdmin(authentication)")
    public ResponseEntity<Void> deleteShopPhoto(@PathVariable Long id,
                            HttpServletRequest httpRequest) throws Exception {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket(ip);
        if (bucket.tryConsume(1)) {
            log.debug("delete shop photo id: {}", id);
            service.deleteForShop(id);
            return ResponseEntity.noContent().build();
        } else {
            log.warn("rate limit exceeded for IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }
}
