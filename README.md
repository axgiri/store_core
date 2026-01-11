# OldLab API (axgiri.com.kz)

Backend microservice for a tech marketplace platform. Handles user authentication, product management, reviews, reports, and serves as an API gateway for the chat microservice.

## Table of Contents

- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Reference](#api-reference)
- [Authentication & Authorization](#authentication--authorization)
- [Inter-Service Communication](#inter-service-communication)
- [Rate Limiting](#rate-limiting)
- [Database Schema](#database-schema)

---

## Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Runtime | Java | 21 |
| Framework | Spring Boot | 3.5.3 |
| Database | PostgreSQL | 13 |
| Cache | Redis | Latest |
| Search Engine | Elasticsearch | Latest |
| Message Broker | Apache Kafka | Latest |
| Object Storage | MinIO | Latest |
| Migrations | Flyway | Latest |
| HTTP Client | OpenFeign | 4.3.0 |
| Reactive Client | WebClient (WebFlux) | - |
| Fault Tolerance | Resilience4j | 1.7.1 |
| Rate Limiting | Bucket4j | - |

---

## Architecture

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Frontend      │────▶│   OldLab API    │────▶│ Notification    │
│   (React)       │     │   (this)        │     │ Service         │
└─────────────────┘     └────────┬────────┘     └─────────────────┘
                                 │
                                 │ WebClient
                                 ▼
                        ┌─────────────────┐
                        │  Chat Service   │
                        │  (WebSocket)    │
                        └─────────────────┘
```

### External Services Integration

| Service | Protocol | Purpose |
|---------|----------|---------|
| **Notification Service** | Feign (HTTP) | Reviews and reports CRUD operations, average ratings |
| **Chat Service** | WebClient (Reactive) | Proxied chat API with JWT validation and `x-user-id` header injection |

---

## Getting Started

### Prerequisites

- JDK 21+
- Docker & Docker Compose
- Maven 3.8+ (or use included `mvnw`)

### Running with Docker Compose (Recommended)

```bash
docker-compose up -d
```

This starts all dependencies:
- PostgreSQL (port 5432)
- Redis (port 6379)
- Kafka (port 9092)
- Elasticsearch (port 9200)
- MinIO (port 9000)
- Application (port 8080)

### Running Locally

Ensure all dependencies are running, then:

```bash
./mvnw spring-boot:run
```

Or with specific profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Building

```bash
./mvnw clean package -DskipTests
```

---

## Configuration

### Environment Variables

Key configuration in `application.yaml`:

| Property | Description | Default |
|----------|-------------|---------|
| `spring.datasource.url` | PostgreSQL connection URL | `jdbc:postgresql://authentication-db:5432/authenticationdb` |
| `spring.data.redis.host` | Redis host | `redis` |
| `spring.kafka.bootstrap-servers` | Kafka brokers | `kafka:9092` |
| `spring.elasticsearch.uris` | Elasticsearch URL | `http://elasticsearch:9200` |
| `minio.url` | MinIO endpoint | `http://minio:9000` |
| `chat.service.url` | Chat microservice URL | `http://chat-service:8082` |
| `jwt.secret.ttl` | Access token TTL (minutes) | `15` |
| `jwt.refresh.ttl` | Refresh token TTL (days) | `30` |

### MinIO Buckets

| Bucket | Purpose |
|--------|---------|
| `default-bucket` | General storage |
| `persons-bucket` | User profile photos |
| `products-bucket` | Product images |

### Kafka Topics

| Topic | Purpose |
|-------|---------|
| `message-topic` | Chat messages |
| `report-topic` | User reports |
| `review-topic` | Product/user reviews |

---

## API Reference

Base URL: `/api/v1`

### Person Controller (`/persons`)

#### Authentication

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/async/signup` | Register new user (async) | No |
| `POST` | `/login` | Authenticate and get tokens | No |
| `POST` | `/refresh` | Refresh access token | No |
| `POST` | `/revoke` | Revoke single refresh token | No |
| `POST` | `/revokeAll` | Revoke all user's refresh tokens | No |
| `GET` | `/validate` | Validate JWT token | Yes |

##### Request/Response Examples

**POST /login**
```json
// Request
{
  "email": "user@example.com",
  "password": "password123"
}

// Response
{
  "access_token": "eyJhbGciOiJIUzI1NiIs...",
  "refresh_token": "dGhpcyBpcyBhIHJlZnJl...",
  "person": {
    "id": 1,
    "first_name": "John",
    "last_name": "Doe",
    "email": "user@example.com",
    "role_enum": "USER"
  }
}
```

**POST /async/signup**
```json
{
  "first_name": "John",
  "last_name": "Doe",
  "email": "user@example.com",
  "password": "password123",
  "phone_number": "+1234567890"
}
```

#### User Management

| Method | Endpoint | Description | Auth | Access |
|--------|----------|-------------|------|--------|
| `GET` | `/me` | Get current authenticated user | Yes | Self |
| `GET` | `/findById/{id}` | Get user by ID | Yes | Any |
| `GET` | `/findByPhoneNumber/{phone}` | Get user by phone | Yes | Moderator, Admin |
| `POST` | `/update/{id}` | Update user profile | Yes | Self, Admin |
| `DELETE` | `/delete/{id}` | Delete user account | Yes | Self, Admin |

#### Password Management

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `PUT` | `/updatePassword` | Change password (authenticated) | Yes |
| `POST` | `/requestPasswordReset` | Request OTP for password reset | No |
| `POST` | `/resetPassword` | Reset password with OTP | No |

---

### Activation Controller (`/activate`)

OTP-based account activation and passwordless login.

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/activate` | Activate account with OTP |
| `POST` | `/send/activate/{email}` | Send activation OTP |
| `POST` | `/resend/activate/{email}` | Resend activation OTP |
| `POST` | `/login` | Login via OTP (passwordless) |
| `POST` | `/send/login/{email}` | Send login OTP |

**POST /activate**
```json
{
  "email": "user@example.com",
  "otp": "123456"
}
```

---

### Product Controller (`/products`)

| Method | Endpoint | Description | Auth | Access |
|--------|----------|-------------|------|--------|
| `POST` | `/` | Create product | Yes | Any |
| `GET` | `/{id}` | Get product by ID | No | Any |
| `GET` | `/list` | List all products (paginated) | No | Any |
| `GET` | `/persons/{personId}` | List user's products | No | Any |
| `PUT` | `/{id}` | Update product | Yes | Owner, Admin |
| `DELETE` | `/{id}` | Delete product | Yes | Owner, Admin |

#### Search Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/search?q={query}` | Full-text search (Elasticsearch) |
| `GET` | `/persons/{id}/search?q={query}` | Search within user's products |
| `GET` | `/list/{category}` | List by category |
| `GET` | `/search/categories/{category}?q={query}` | Search within category |

#### Query Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | 0 | Page number (0-indexed) |
| `size` | int | 20 | Page size |
| `q` | string | - | Search query |

#### Product Categories

```
LAPTOPS, PC, PSCOMPONENTS, TABLETS, SMARTPHONES, ACCESSORIES, DEVICES, OTHERS
```

**POST / (Create Product)**
```json
{
  "name": "MacBook Pro 16",
  "description": "Apple M3 Pro, 18GB RAM",
  "price": 2499.99,
  "category": "LAPTOPS",
  "tags": ["apple", "laptop", "m3"],
  "hidden_labels": ["premium"],
  "attributes": {
    "brand": "Apple",
    "year": "2024"
  }
}
```

---

### Photo Controller (`/photos`)

Handles image upload/download with automatic WebP conversion. Max 5 photos per product.

| Method | Endpoint | Description | Auth | Access |
|--------|----------|-------------|------|--------|
| `PUT` | `/persons/{id}` | Upload profile photo | Yes | Self, Admin |
| `GET` | `/persons/{id}` | Get profile photo | No | Any |
| `DELETE` | `/persons/{id}` | Delete profile photo | Yes | Self, Admin |
| `POST` | `/products/{id}` | Upload product photo | Yes | Owner, Admin |
| `GET` | `/products/{id}` | Get product photos | No | Any |
| `DELETE` | `/products/{productId}/{objectKey}` | Delete product photo | Yes | Owner, Admin |

**Request**: `multipart/form-data` with `file` field

**Response** (GET /persons/{id}): `image/webp` binary

---

### Review Controller (`/reviews`)

| Method | Endpoint | Description | Auth | Access |
|--------|----------|-------------|------|--------|
| `POST` | `/person` | Create review for user | Yes | Self (as author) |
| `GET` | `/person/{personId}` | Get reviews about user | No | Any |
| `GET` | `/rate/person/{personId}` | Get user's average rating | No | Any |
| `GET` | `/author/{authorId}` | Get reviews by author | No | Any |
| `GET` | `/` | List all reviews (paginated) | No | Any |
| `DELETE` | `/{id}` | Delete review | Yes | Owner, Moderator, Admin |

**POST /person**
```json
{
  "author_id": 1,
  "person_id": 2,
  "rating": 5,
  "comment": "Great seller!"
}
```

**GET /rate/person/{id} Response**
```json
{
  "average_rating": 4.5,
  "total_reviews": 42
}
```

---

### Report Controller (`/reports`)

User reporting system for moderation.

| Method | Endpoint | Description | Auth | Access |
|--------|----------|-------------|------|--------|
| `POST` | `/create` | Create report | Yes | Self (as reporter) |
| `GET` | `/` | List all reports | Yes | Moderator, Admin |
| `GET` | `/{id}` | Get report by ID | Yes | Moderator, Admin |
| `GET` | `/status/{status}` | Get reports by status | Yes | Moderator, Admin |
| `GET` | `/author/{authorId}` | Get reports by author | Yes | Self, Moderator, Admin |
| `PATCH` | `/{reportId}/status?status={status}` | Update report status | Yes | Moderator, Admin |

**Report Statuses**: `PENDING`, `REVIEWED`, `RESOLVED`, `REJECTED`

**Report Reasons**: Various predefined reasons

---

### Chat Proxy Controller (`/chat`)

Proxies requests to Chat Service with JWT validation and user ID injection.

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/` | Get user's chats |
| `POST` | `/create/{recipientId}` | Create/get chat with user |
| `GET` | `/{chatId}/messages` | Get chat messages |
| `GET` | `/{chatId}/messages/unread/count` | Get unread count |
| `PUT` | `/{chatId}/read` | Mark messages as read |
| `POST` | `/{chatId}/send` | Send message |
| `PUT` | `/{chatId}/messages/{messageId}` | Edit message |
| `DELETE` | `/{chatId}/messages/{messageId}` | Delete message |

All requests require `Authorization: Bearer <token>` header. The proxy:
1. Validates JWT token
2. Extracts and caches `userId` (15 min TTL)
3. Forwards request to Chat Service with `x-user-id` header

---

## Authentication & Authorization

### JWT Token Structure

- **Access Token**: Short-lived (15 min), used for API requests
- **Refresh Token**: Long-lived (30 days), used to obtain new access tokens

### Role-Based Access Control

| Role | Permissions |
|------|-------------|
| `USER` | Own resources, create content |
| `MODERATOR` | User permissions + view reports, manage reviews |
| `ADMIN` | Full access to all resources |

### OAuth2 (Google)

Supports Google OAuth2 login. After successful authentication, redirects to frontend with tokens.

```
GET /login/oauth2/code/google
→ Redirect to: {frontend.redirect.url}/oauth-success?token=...
```

---

## Inter-Service Communication

### Notification Service (Feign Client)

```java
@FeignClient(name = "notification-service", url = "http://notification-service:8081/api/notifications")
```

Used for:
- Fetching reviews and ratings
- Fetching reports
- Checking if user has reviewed another user

### Chat Service (WebClient)

Reactive HTTP client with request proxying:

```java
WebClient.builder()
    .method(method)
    .uri(chatServiceUrl + path)
    .header("x-user-id", userId)
    .retrieve()
```

---

## Rate Limiting

Implemented using Bucket4j with per-IP limiting:

| Parameter | Value |
|-----------|-------|
| Window Size | 1 second |
| Max Requests per Window | 50 |

Response on limit exceeded: `429 Too Many Requests`

---

## Database Schema

### Entity: Person

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT |
| `version` | BIGINT | Optimistic locking |
| `first_name` | VARCHAR | NOT NULL |
| `last_name` | VARCHAR | NOT NULL |
| `email` | VARCHAR | UNIQUE, NOT NULL |
| `phone_number` | VARCHAR | UNIQUE |
| `password` | VARCHAR | NOT NULL (BCrypt) |
| `role_enum` | ENUM | USER/MODERATOR/ADMIN |
| `is_active` | BOOLEAN | NOT NULL |
| `is_not_blocked` | BOOLEAN | NOT NULL |
| `created_at` | TIMESTAMP | |
| `updated_at` | TIMESTAMP | |

### Entity: Product

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT |
| `version` | BIGINT | Optimistic locking |
| `name` | VARCHAR | NOT NULL |
| `description` | TEXT | |
| `price` | DECIMAL(19,2) | NOT NULL |
| `category` | ENUM | NOT NULL |
| `is_available` | BOOLEAN | NOT NULL |
| `person_id` | BIGINT | FK → persons |

**Related Collections**:
- `product_tags` (List<String>)
- `product_hidden_labels` (Set<String>)
- `product_attributes` (Map<String, String>)

### Entity: Photo

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK |
| `object_key` | VARCHAR | MinIO object key |
| `product_id` | BIGINT | FK → products |

---

## Error Handling

### Standard Error Response

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/persons/signup"
}
```

### HTTP Status Codes

| Code | Meaning |
|------|---------|
| `200` | Success |
| `201` | Created |
| `202` | Accepted (async operations) |
| `204` | No Content |
| `400` | Bad Request (validation errors) |
| `401` | Unauthorized |
| `403` | Forbidden |
| `404` | Not Found |
| `429` | Too Many Requests |
| `500` | Internal Server Error |

---

## Circuit Breaker Configuration

Resilience4j circuit breaker for external service calls:

| Parameter | Value |
|-----------|-------|
| Sliding Window Size | 10 |
| Minimum Calls | 5 |
| Failure Rate Threshold | 50% |
| Wait Duration in Open State | 5s |
| Permitted Calls in Half-Open | 3 |
