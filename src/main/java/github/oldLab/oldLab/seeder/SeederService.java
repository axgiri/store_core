package github.oldLab.oldLab.seeder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import github.oldLab.oldLab.entity.Activates;
import github.oldLab.oldLab.entity.Person;
import github.oldLab.oldLab.entity.Product;
import github.oldLab.oldLab.repository.ActivateRepository;
import github.oldLab.oldLab.repository.PersonRepository;
import github.oldLab.oldLab.repository.PhotoRepository;
import github.oldLab.oldLab.repository.ProductRepository;
import github.oldLab.oldLab.repository.RefreshTokenRepository;
import github.oldLab.oldLab.search.ProductDocumentRequest;
import github.oldLab.oldLab.search.ProductSearchRepository;
import github.oldLab.oldLab.seeder.factory.ActivateFactory;
import github.oldLab.oldLab.seeder.factory.PersonFactory;
import github.oldLab.oldLab.seeder.factory.PhotoFactory;
import github.oldLab.oldLab.seeder.factory.ProductFactory;
import github.oldLab.oldLab.seeder.factory.RefreshTokenFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;

/**
 * Seeder Service - Generates fake data for development environment
 * This service is ONLY active when Spring profile "dev" is enabled
 * Production will NOT load this bean
 */

@Slf4j
@Service
@Profile("dev")  // ← Only active in development environment
@RequiredArgsConstructor
public class SeederService {

    private final PersonFactory personFactory;
    private final ProductFactory productFactory;
    private final ActivateFactory activateFactory;
    private final PhotoFactory photoFactory;
    private final RefreshTokenFactory refreshTokenFactory;

    private final PersonRepository personRepository;
    private final ProductRepository productRepository;
    private final ActivateRepository activateRepository;
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
    var activates = new ArrayList<Activates>(count);
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
        // Refresh tokens for subset of persons
        persons.stream().limit(Math.max(1, count)).forEach(p -> refreshTokenRepository.save(refreshTokenFactory.create(p)));

        long total = persons.size() + activates.size() + products.size();
        log.info("Seeding complete. Total persisted (excluding photos & tokens): {}", total);
        return total;
    }
}
