package github.oldLab.oldLab.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import github.oldLab.oldLab.service.PhotoService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService service;

    @PutMapping(path = "/persons/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void uploadPersonPhoto(@PathVariable Long id,@RequestPart("file") MultipartFile file) throws Exception {
        service.uploadForPerson(id, file);
    }

    @GetMapping("/persons/{id}")
    public ResponseEntity<byte[]> getPersonPhoto(@PathVariable Long id) {
        byte[] bytes = service.loadForPerson(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .cacheControl(CacheControl.noCache())
                .body(bytes);
    }

    @PutMapping(path = "/shops/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void uploadShopPhoto(@PathVariable Long id,@RequestPart("file") MultipartFile file) throws Exception {
        service.uploadForShop(id, file);
    }

    @GetMapping("/shops/{id}")
    public ResponseEntity<byte[]> getShopPhoto(@PathVariable Long id) {
        byte[] bytes = service.loadForShop(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .cacheControl(CacheControl.noCache())
                .body(bytes);
    }
}
