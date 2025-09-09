package github.oldLab.oldLab.seeder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import github.oldLab.oldLab.Enum.ReportTypeEnum;
import github.oldLab.oldLab.entity.Person;
import github.oldLab.oldLab.entity.Product;
import github.oldLab.oldLab.entity.Shop;
import github.oldLab.oldLab.repository.*;
import github.oldLab.oldLab.seeder.factory.ActivateFactory;
import github.oldLab.oldLab.seeder.factory.PersonFactory;
import github.oldLab.oldLab.seeder.factory.PhotoFactory;
import github.oldLab.oldLab.seeder.factory.ProductFactory;
import github.oldLab.oldLab.seeder.factory.RefreshTokenFactory;
import github.oldLab.oldLab.seeder.factory.ReportFactory;
import github.oldLab.oldLab.seeder.factory.ReviewFactory;
import github.oldLab.oldLab.seeder.factory.ShopFactory;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeederService {

    private static final Logger log = LoggerFactory.getLogger(SeederService.class);

    private final PersonFactory personFactory;
    private final ShopFactory shopFactory;
    private final ProductFactory productFactory;
    private final ReviewFactory reviewFactory;
    private final ActivateFactory activateFactory;
    private final ReportFactory reportFactory;
    private final PhotoFactory photoFactory;
    private final RefreshTokenFactory refreshTokenFactory;

    private final PersonRepository personRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final ActivateRepository activateRepository;
    private final ReportRepository reportRepository;
    private final PhotoRepository photoRepository;
    private final RefreshTokenRepository refreshTokenRepository;

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

        // Activate (independent of persons but could match phone numbers randomly)
    var activates = new ArrayList<github.oldLab.oldLab.entity.Activate>(count);
        for (int i = 0; i < count; i++) {
            activates.add(activateFactory.create());
        }
        activateRepository.saveAll(activates);

        // Shops owned by random persons
        List<Shop> shops = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            var owner = persons.get(ThreadLocalRandom.current().nextInt(persons.size()));
            shops.add(shopFactory.create(owner));
        }
        shops = shopRepository.saveAll(shops);

        // Products for each shop (count each)
        List<Product> products = new ArrayList<>(count * shops.size());
        for (Shop s : shops) {
            for (int i = 0; i < count; i++) {
                products.add(productFactory.create(s));
            }
        }
        products = productRepository.saveAll(products);

        // Photos for subset of persons and shops
        persons.stream().limit(Math.max(1, count / 2)).forEach(p -> photoRepository.save(photoFactory.create(p)));
        shops.stream().limit(Math.max(1, count / 2)).forEach(sh -> photoRepository.save(photoFactory.create(sh)));

        // Reviews (randomized)
    var reviews = new ArrayList<github.oldLab.oldLab.entity.Review>(count);
        for (int i = 0; i < count; i++) {
            var author = persons.get(ThreadLocalRandom.current().nextInt(persons.size()));
            var maybePerson = persons.get(ThreadLocalRandom.current().nextInt(persons.size()));
            var shop = shops.get(ThreadLocalRandom.current().nextInt(shops.size()));
            reviews.add(reviewFactory.create(author, maybePerson, shop));
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
                case SHOP -> targetId = shops.get(ThreadLocalRandom.current().nextInt(shops.size())).getId();
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

        long total = persons.size() + activates.size() + shops.size() + products.size() + reviews.size() + reports.size();
        log.info("Seeding complete. Total persisted (excluding photos & tokens): {}", total);
        return total;
    }
}
