package github.oldLab.oldLab.seeder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/seeder")
@RequiredArgsConstructor
public class SeederController {

    private static final Logger log = LoggerFactory.getLogger(SeederController.class);

    private final SeederService seederService;

    @PostMapping("/seed")
    @PreAuthorize("@accessControlService.isAdmin(authentication)")
    public ResponseEntity<String> seed(@RequestParam(name = "count", defaultValue = "10") int count) {
        log.info("Received seed request count={}", count);
        long total = seederService.seedAll(count);
        return ResponseEntity.ok("Seeded " + count + " records per entity. Total persisted (core entities)=" + total);
    }
}
