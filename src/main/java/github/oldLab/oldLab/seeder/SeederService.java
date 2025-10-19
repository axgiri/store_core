package github.oldLab.oldLab.seeder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import github.oldLab.oldLab.Enum.ReportTypeEnum;
import github.oldLab.oldLab.entity.Person;
import github.oldLab.oldLab.entity.Product;
import github.oldLab.oldLab.repository.ActivateRepository;
import github.oldLab.oldLab.repository.PersonRepository;
import github.oldLab.oldLab.repository.PhotoRepository;
import github.oldLab.oldLab.repository.ProductRepository;
import github.oldLab.oldLab.repository.RefreshTokenRepository;
import github.oldLab.oldLab.repository.ReportRepository;
import github.oldLab.oldLab.repository.ReviewRepository;
import github.oldLab.oldLab.search.ProductDocumentRequest;
import github.oldLab.oldLab.search.ProductSearchRepository;
import github.oldLab.oldLab.seeder.factory.ActivateFactory;
import github.oldLab.oldLab.seeder.factory.PersonFactory;
import github.oldLab.oldLab.seeder.factory.PhotoFactory;
import github.oldLab.oldLab.seeder.factory.ProductFactory;
import github.oldLab.oldLab.seeder.factory.RefreshTokenFactory;
import github.oldLab.oldLab.seeder.factory.ReportFactory;
import github.oldLab.oldLab.seeder.factory.ReviewFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

/**
 * Seeder Service - Generates fake data for development environment
 * This service is ONLY active when Spring profile "dev" is enabled
 * Production will NOT load this bean
 */
@Service
@Profile("dev")  // ← Only active in development environment
@RequiredArgsConstructor
public class SeederService {

    private static final Logger log = LoggerFactory.getLogger(SeederService.class);

    private final PersonFactory personFactory;
    private final ProductFactory productFactory;
    private final ReviewFactory reviewFactory;
    private final ActivateFactory activateFactory;
    private final ReportFactory reportFactory;
    private final PhotoFactory photoFactory;
    private final RefreshTokenFactory refreshTokenFactory;

    private final PersonRepository personRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final ActivateRepository activateRepository;
    private final ReportRepository reportRepository;
    private final PhotoRepository photoRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ProductSearchRepository productSearchRepository;

    @Value("${max.photo.per.product}")
    private int maxPhotoPerProduct;

    @Transactional
    public long seedAll(int count) {
        if (count <= 0) {
            return 0L;
        }
        log.info("Seeding {} records per entity", count);

        // Persons
        List<Person> persons = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            persons.add(personFactory.create());
        }
        persons = personRepository.saveAll(persons);

    // Activate (independent of persons; uses emails in the new flow)
    var activates = new ArrayList<github.oldLab.oldLab.entity.Activate>(count);
        for (int i = 0; i < count; i++) {
            activates.add(activateFactory.create());
        }
        activateRepository.saveAll(activates);

        // Products for each person (count each)
        List<Product> products = new ArrayList<>(count * persons.size());
        for (Person s : persons) {
            for (int i = 0; i < count; i++) {
                products.add(productFactory.create(s));
            }
        }
        products = productRepository.saveAll(products);
        productSearchRepository.saveAll(products.stream().map(ProductDocumentRequest::fromEntity).toList());

        // Photos for products (random count per product up to configured max)
        if (maxPhotoPerProduct > 0) {
            products.forEach(prod -> {
                int toCreate = ThreadLocalRandom.current().nextInt(maxPhotoPerProduct + 1); // 0..max
                for (int i = 0; i < toCreate; i++) {
                    photoRepository.save(photoFactory.create(prod));
                }
            });
        }

        // Photos for subset of persons
        persons.stream().limit(Math.max(1, count / 2)).forEach(p -> photoRepository.save(photoFactory.create(p)));

        // Reviews (randomized)
    var reviews = new ArrayList<github.oldLab.oldLab.entity.Review>(count);
        for (int i = 0; i < count; i++) {
            var author = persons.get(ThreadLocalRandom.current().nextInt(persons.size()));
            var maybePerson = persons.get(ThreadLocalRandom.current().nextInt(persons.size()));
            reviews.add(reviewFactory.create(author, maybePerson));
        }
        reviewRepository.saveAll(reviews);

        // Reports referencing random targets
    var reports = new ArrayList<github.oldLab.oldLab.entity.Report>(count);
        for (int i = 0; i < count; i++) {
            var reporter = persons.get(ThreadLocalRandom.current().nextInt(persons.size()));
            var typeIdx = ThreadLocalRandom.current().nextInt(ReportTypeEnum.values().length);
            var type = ReportTypeEnum.values()[typeIdx];
            Long targetId;
            switch (type) {
                case USER -> targetId = persons.get(ThreadLocalRandom.current().nextInt(persons.size())).getId();
                case REVIEW -> targetId = reviews.isEmpty() ? null : reviews.get(ThreadLocalRandom.current().nextInt(reviews.size())).getId();
                default -> targetId = null;
            }
            if (targetId != null) { // ensure not null
                reports.add(reportFactory.create(reporter, type, targetId));
            }
        }
        reportRepository.saveAll(reports);

        // Refresh tokens for subset of persons
        persons.stream().limit(Math.max(1, count)).forEach(p -> refreshTokenRepository.save(refreshTokenFactory.create(p)));

        long total = persons.size() + activates.size() + products.size() + reviews.size() + reports.size();
        log.info("Seeding complete. Total persisted (excluding photos & tokens): {}", total);
        return total;
    }
}
