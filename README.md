[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=pk-8eee68de-02d9-40d5-918c-252679469297&metric=reliability_rating)](https://sonarcloud.io/summary/overall?id=pk-8eee68de-02d9-40d5-918c-252679469297&branch=master) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=pk-8eee68de-02d9-40d5-918c-252679469297&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=pk-8eee68de-02d9-40d5-918c-252679469297) [![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=pk-8eee68de-02d9-40d5-918c-252679469297&metric=duplicated_lines_density)](https://sonarcloud.io/summary/overall?id=pk-8eee68de-02d9-40d5-918c-252679469297&branch=master) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=pk-8eee68de-02d9-40d5-918c-252679469297&metric=coverage)](https://sonarcloud.io/summary/overall?id=pk-8eee68de-02d9-40d5-918c-252679469297&branch=master) [![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=pk-8eee68de-02d9-40d5-918c-252679469297&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=pk-8eee68de-02d9-40d5-918c-252679469297&branch=master) [![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=pk-8eee68de-02d9-40d5-918c-252679469297&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=pk-8eee68de-02d9-40d5-918c-252679469297&branch=master)
# Store Core API ([axgiri.tech](https://axgiri.tech))

store_core is the main business service of the marketplace. It implements product domain logic, profile related operations, media workflows, and user generated feedback entities.

## Status

Primary domain service. Most user visible marketplace behavior is resolved here.

### Role in the architecture

store_core is the central domain orchestrator. It owns business entities and exposes the main API surface consumed by clients through gateway routes. It coordinates with auth for trust boundaries and with notification components for side effect workflows.

This module defines how marketplace behavior works from a product perspective.

### Functional scope

- manages person profile data in business context
- manages product lifecycle from creation to removal
- handles product and profile media attachments
- accepts and stores reviews
- accepts and stores reports for moderation scenarios
- provides admin focused moderation endpoints

### API contract highlights

- person domain routes are grouped under `/api/v1/persons`
- product domain routes are grouped under `/api/v1/products`
- media routes are grouped under `/api/v1/photos`
- review and report routes are grouped under `/api/v1/reviews` and `/api/v1/reports`
- admin moderation routes are grouped under `/api/v1/admin/sc/*`

### Domain orchestration model

- validates and coordinates identity related checks through store_auth integration
- emits and consumes Kafka events for asynchronous side effects
- stores binary media in MinIO while keeping business metadata in relational storage
- offloads full text and filtered product discovery to Elasticsearch

### Data and integrations

- PostgreSQL stores transactional domain records
- Redis supports cache and fast lookup paths
- Elasticsearch supports product search use cases
- MinIO stores binary objects such as photos
- Kafka transports domain events between services
- Flyway manages schema version evolution
- Resilience4j protects outbound integration calls

### Tech Stack

- Java 25 with preview features enabled
- Spring Boot 4.0.1
- Spring Web and Spring Validation
- Spring Data JPA
- PostgreSQL
- Redis
- Elasticsearch
- MinIO
- Kafka
- Flyway
- Resilience4j
- Micrometer with Prometheus and OpenTelemetry

### Platform impact

store_core is the backbone of marketplace behavior. It is the module where business rules are enforced and where most user visible operations are resolved.

If store_core is unavailable, the marketplace can still authenticate users, but profile updates, product operations, and moderation workflows cannot progress.

## All microservices

- https://github.com/axgiri/store-jwt-spring-boot-starter
- https://github.com/axgiri/store_gateway
- https://github.com/axgiri/store_infrastructure
- https://github.com/axgiri/store_auth
- https://github.com/axgiri/store_core
- https://github.com/axgiri/store_chat
- https://github.com/Scheldie/Notification_Reports

## SonarQube

Run static analysis with the Maven Sonar goal:

```bash
./mvnw -Psonar verify sonar:sonar -Dsonar.token=token
```

## k6 Load Testing Guide

This service has a ready-to-run k6 setup in `k6/`.

### Prerequisites

- Docker and Docker Compose
- GNU make
- k6 CLI installed locally
- `store_core/.env` with:
	- `GITHUB_PACKAGES_USER`
	- `GITHUB_PACKAGES_TOKEN`

### Quick Run

Run the full flow (up, readiness checks, test, cleanup):

```bash
make -f k6/main.mk run
```

### Step-by-Step Run

Use this flow when debugging or tuning:

```bash
make -f k6/main.mk up
make -f k6/main.mk wait-schema
make -f k6/main.mk wait-app
make -f k6/main.mk run-test
make -f k6/main.mk down
```

Notes:

- `up` automatically prepares `store_auth` test dependencies via `k6/auth.mk`.
- Short `curl: (56) Recv failure: Connection reset by peer` lines can appear during warm-up.
- `.envK6` for this module is in `store_core/k6/helpers/.envK6`.

### k6 Tuning Knobs

Common variables in `store_core/k6/helpers/.envK6`:

- `K6_VUS_PER_SCENARIO`
- `K6_DEFAULT_DURATION`
- `K6_ENABLE_PRODUCTS_CRUD_SCENARIO`
- `K6_ENABLE_PRODUCT_PHOTOS_SCENARIO`
- `PERSONS_ME_ALLOW_404`

### Capacity Template (VM: 4 vCPU, 4GB DDR4)

Fill this section after your benchmark run. Replace every `X` with your measured value.

| Metric | Value |
| --- | --- |
| VM profile | 4 vCPU / 4GB DDR4 |
| Test duration | 5m |
| Virtual users | 4000 |
| Total requests | 2546389 |
| Throughput (req/s) | 8465.629507/s |
| p95 latency (ms) | 411.67ms |
| p90 latency (ms) | 301.75ms |
| Error rate (%) | 0 |