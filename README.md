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
