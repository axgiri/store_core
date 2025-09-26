package github.oldLab.oldLab;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import github.oldLab.oldLab.seeder.SeederService;

/**
 * Load-style functional test that seeds data and issues jittered, weighted requests across all controllers
 * except the SeederController. Security is disabled via TestSecurityConfig so no JWTs are required.
 *
 * Note: this test is intended to be run manually. The user requested not to run build/execute now.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestSecurityConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LoadFunctionalTest {

    private static final Logger log = LoggerFactory.getLogger(LoadFunctionalTest.class);

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SeederService seederService;

    @Autowired
    private github.oldLab.oldLab.repository.PersonRepository personRepository;

    @Autowired
    private github.oldLab.oldLab.repository.ShopRepository shopRepository;

    @Autowired
    private github.oldLab.oldLab.repository.ProductRepository productRepository;

    @Autowired
    private github.oldLab.oldLab.repository.ReviewRepository reviewRepository;

    @Autowired
    private org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping handlerMapping;

    private final List<Endpoint> endpoints = new ArrayList<>();

    private final Random rnd = ThreadLocalRandom.current();

    @BeforeAll
    public void setup() {
    // seed the DB with a modest amount of data for controllers to operate on
    seederService.seedAll(5);

        // collect some real ids to reduce 404s
        Long samplePersonId = personRepository.findAll().stream().findFirst().map(p -> p.getId()).orElse(1L);
        Long sampleShopId = shopRepository.findAll().stream().findFirst().map(s -> s.getId()).orElse(1L);
        Long sampleProductId = productRepository.findAll().stream().findFirst().map(p -> p.getId()).orElse(1L);
        Long sampleReviewId = reviewRepository.findAll().stream().findFirst().map(r -> r.getId()).orElse(1L);

        // Dynamically discover controller request mappings and build endpoints to exercise
        var map = handlerMapping.getHandlerMethods();
        map.forEach((key, value) -> {
            try {
                var patternsCond = key.getPatternsCondition();
                var methodsCond = key.getMethodsCondition();

                if (patternsCond == null) {
                    // nothing to iterate for this mapping
                    return;
                }

                var patterns = patternsCond.getPatterns();
                var methods = methodsCond == null ? java.util.Set.<org.springframework.web.bind.annotation.RequestMethod>of() : methodsCond.getMethods();

                for (String pattern : patterns) {
                    try {
                        // skip seeder endpoints and actuator/management if present
                        if (pattern.startsWith("/api/v1/seeder") || pattern.startsWith("/actuator") || pattern.startsWith("/management")) {
                            continue;
                        }

                        // replace path variables like {id} with real sample ids when possible
                        String patternReplaced = pattern.replaceAll("\\{.+?\\}", "1");
                        String sample0 = patternReplaced;
                        if (sample0.contains("/persons/") || sample0.contains("/persons")) {
                            sample0 = sample0.replaceAll("1", samplePersonId.toString());
                        }
                        if (sample0.contains("/shops/") || sample0.contains("/shops")) {
                            sample0 = sample0.replaceAll("1", sampleShopId.toString());
                        }
                        if (sample0.contains("/products/") || sample0.contains("/products")) {
                            sample0 = sample0.replaceAll("1", sampleProductId.toString());
                        }
                        if (sample0.contains("/reviews/") || sample0.contains("/reviews")) {
                            sample0 = sample0.replaceAll("1", sampleReviewId.toString());
                        }
                        final String sample = sample0; // final for nested lambdas

                        // determine method weights: GET more frequent than POST/PUT/DELETE
                        if (methods.isEmpty()) {
                            endpoints.add(new Endpoint(HttpMethod.GET, sample, 2));
                        } else {
                            methods.forEach(m -> {
                                try {
                                    int weight = switch (m.name()) {
                                        case "GET" -> 5;
                                        case "POST" -> 3;
                                        case "PUT" -> 2;
                                        case "DELETE" -> 1;
                                        default -> 1;
                                    };
                                    // for endpoints that expect body, set small sample JSON when obvious
                                    String body = null;
                                    if (m.name().equals("POST") || m.name().equals("PUT")) {
                                        // build realistic payloads for common endpoints
                                        if (sample.contains("/persons/async/signup") || sample.contains("/persons/async/signup")) {
                                            // sample signup payload
                                            body = String.format("{\"phoneNumber\":\"+1000000%04d\",\"firstName\":\"Load\",\"lastName\":\"Test\",\"email\":\"load%d@example.com\"}", rnd.nextInt(9999), rnd.nextInt(9999));
                                        } else if (sample.contains("/persons/login")) {
                                            body = String.format("{\"phoneNumber\":\"%s\",\"password\":\"%s\"}", personRepository.findAll().stream().findFirst().map(p -> p.getPhoneNumber()).orElse("+10000000001"), "pass");
                                        } else if (sample.contains("/reviews/create") || sample.contains("/reviews")) {
                                            body = String.format("{\"shopId\":%d,\"rating\":%d,\"comment\":\"Nice%03d\"}", sampleShopId, 4 + rnd.nextInt(2), rnd.nextInt(999));
                                        } else if (sample.contains("/reports/create") || sample.contains("/reports")) {
                                            body = String.format("{\"targetId\":%d,\"type\":\"SHOP\",\"reason\":\"spam\"}", sampleShopId);
                                        } else if (sample.contains("/products")) {
                                            body = "{}"; // generic
                                        } else {
                                            body = "{}";
                                        }
                                    }
                                    endpoints.add(new Endpoint(HttpMethod.valueOf(m.name()), sample, weight, body));
                                } catch (Exception e) {
                                    log.warn("failed to add endpoint for method {} on pattern {}: {}", m, sample, e.toString());
                                }
                            });
                        }
                    } catch (Exception e) {
                        log.warn("skipping mapping pattern processing due to error for pattern {}: {}", pattern, e.toString());
                    }
                }
            } catch (Exception outer) {
                log.warn("skipping handler mapping entry due to error: {}", outer.toString());
            }
        });

        // If discovery produced no endpoints (unexpected), fall back to a small default set
        if (endpoints.isEmpty()) {
            endpoints.add(new Endpoint(HttpMethod.GET, "/api/v1/products/list", 5));
        }
    }

    @AfterAll
    public void tearDown() {
        log.info("LoadFunctionalTest completed");
    }

    @Test
    public void runLoadStyleTraffic() throws Exception {
        final int threads = 8;
        final int durationSeconds = 20; // short by default
        final ExecutorService exec = Executors.newFixedThreadPool(threads);

        final AtomicInteger requests = new AtomicInteger();
        final long endAt = System.nanoTime() + TimeUnit.SECONDS.toNanos(durationSeconds);

        log.info("Starting load run against port {} for {}s using {} threads and {} endpoints", port, durationSeconds, threads, endpoints.size());

        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            tasks.add(() -> {
                while (System.nanoTime() < endAt) {
                    Endpoint ep = pickWeightedEndpoint();
                    try {
                        long beforeMemory = currentUsedMemory();
                        issueRequest(ep);
                        long afterMemory = currentUsedMemory();
                        if (requests.incrementAndGet() % 50 == 0) {
                            log.info("requests={} mem-used-before={}kb mem-used-after={}kb", requests.get(), beforeMemory/1024, afterMemory/1024);
                        }
                    } catch (Exception e) {
                        log.warn("request failed: {}", e.toString());
                    }

                    // jitter between requests to simulate real usage (10..300ms)
                    Thread.sleep(10 + rnd.nextInt(300));
                }
                return null;
            });
        }

    List<Future<Void>> futures = exec.invokeAll(tasks, durationSeconds + 5, TimeUnit.SECONDS);
    exec.shutdownNow();
    exec.awaitTermination(5, TimeUnit.SECONDS);

    log.info("Load run finished. total requests sent (approx): {}", requests.get());
    log.debug("futures returned: {}", futures == null ? 0 : futures.size());

        // Simple memory snapshot after run
        long usedKb = currentUsedMemory() / 1024;
        log.info("Memory used after run: {} KB", usedKb);

        // basic assertion to ensure test ran and made some requests
        assertThat(requests.get()).isGreaterThan(0);
    }

    private void issueRequest(Endpoint ep) {
        String url = "http://localhost:" + port + ep.path;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");
        HttpEntity<String> entity = ep.body != null ? new HttpEntity<>(ep.body, headers) : new HttpEntity<>(headers);

        ResponseEntity<String> res = restTemplate.exchange(URI.create(url), ep.method, entity, String.class);
        // we don't fail on non-2xx here; load test should continue
            if (res != null) {
                // noop: touch response to avoid unused-value complaint
                res.getStatusCode().value();
            }
    }

    private Endpoint pickWeightedEndpoint() {
        int total = endpoints.stream().mapToInt(e -> e.weight).sum();
        int r = rnd.nextInt(total);
        int acc = 0;
        for (Endpoint e : endpoints) {
            acc += e.weight;
            if (r < acc) return e;
        }
        return endpoints.get(0);
    }

    private long currentUsedMemory() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    record Endpoint(HttpMethod method, String path, int weight, String body) {
        Endpoint(HttpMethod method, String path, int weight) {
            this(method, path, weight, null);
        }
    }
}
