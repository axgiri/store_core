package github.oldLab.oldLab.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import github.oldLab.oldLab.entity.Person;
import github.oldLab.oldLab.service.UserIdCacheService;
import github.oldLab.oldLab.serviceImpl.TokenServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Proxy controller for chat microservice.
 * Validates JWT tokens, extracts userId, caches it, and forwards requests
 * to the chat microservice with x-user-id header for security.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatProxyController {

    private final WebClient.Builder webClientBuilder;
    private final TokenServiceImpl tokenService;
    private final UserIdCacheService userIdCacheService;

    @Value("${chat.service.url}")
    private String chatServiceUrl;

    /**
     * Get user's chats with pagination
     */
    @GetMapping
    public Mono<ResponseEntity<String>> getChats(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestParam(required = false) String beforeChatId,
            @RequestParam(defaultValue = "50") Integer size) {
        
        return forwardRequest(authHeader, "/ws/v1/chat", "GET", null, 
            builder -> {
                if (beforeChatId != null) {
                    builder.queryParam("beforeChatId", beforeChatId);
                }
                builder.queryParam("size", size);
            });
    }

    /**
     * Get messages for a specific chat
     */
    @GetMapping("/{chatId}/messages")
    public Mono<ResponseEntity<String>> getMessages(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable String chatId,
            @RequestParam(required = false) Long beforeTimestamp,
            @RequestParam(defaultValue = "50") Integer size) {
        
        return forwardRequest(authHeader, "/ws/v1/chat/" + chatId + "/messages", "GET", null,
            builder -> {
                if (beforeTimestamp != null) {
                    builder.queryParam("beforeTimestamp", beforeTimestamp);
                }
                builder.queryParam("size", size);
            });
    }

    /**
     * Get unread messages count for a chat
     */
    @GetMapping("/{chatId}/messages/unread/count")
    public Mono<ResponseEntity<String>> getUnreadMessagesCount(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable String chatId) {
        
        return forwardRequest(authHeader, "/ws/v1/chat/" + chatId + "/messages/unread/count", "GET", null, null);
    }

    /**
     * Mark messages as read
     */
    @PutMapping("/{chatId}/read")
    public Mono<ResponseEntity<String>> markMessagesAsRead(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable String chatId) {
        
        return forwardRequest(authHeader, "/ws/v1/chat/" + chatId + "/read", "PUT", null, null);
    }

    /**
     * Send message to chat
     */
    @PostMapping("/{chatId}/send")
    public Mono<ResponseEntity<String>> sendMessage(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable String chatId,
            @RequestBody String requestBody) {
        
        return forwardRequest(authHeader, "/ws/v1/chat/" + chatId + "/send", "POST", requestBody, null);
    }

    /**
     * Edit message
     */
    @PutMapping("/{chatId}/messages/{messageId}")
    public Mono<ResponseEntity<String>> editMessage(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable String chatId,
            @PathVariable String messageId,
            @RequestBody String requestBody) {
        
        return forwardRequest(authHeader, "/ws/v1/chat/" + chatId + "/messages/" + messageId, 
            "PUT", requestBody, null);
    }

    /**
     * Delete message
     */
    @DeleteMapping("/{chatId}/messages/{messageId}")
    public Mono<ResponseEntity<String>> deleteMessage(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable String chatId,
            @PathVariable String messageId) {
        
        return forwardRequest(authHeader, "/ws/v1/chat/" + chatId + "/messages/" + messageId, 
            "DELETE", null, null);
    }

    /**
     * Forward request to chat microservice with userId from JWT
     */
    private Mono<ResponseEntity<String>> forwardRequest(
            String authHeader,
            String path,
            String method,
            String body,
            java.util.function.Consumer<org.springframework.web.util.UriComponentsBuilder> uriCustomizer) {
        
        try {
            // Extract token
            String token = extractToken(authHeader);
            if (token == null) {
                log.warn("Missing or invalid Authorization header");
                return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"Missing or invalid Authorization header\"}"));
            }

            // Try to get userId from cache first
            Long userId = userIdCacheService.getCachedUserId(token);
            
            if (userId == null) {
                // Cache miss - extract userId from token and cache it
                try {
                    userId = extractUserIdFromToken(token);
                    userIdCacheService.cacheUserId(token, userId);
                    log.debug("Cached userId {} for token", userId);
                } catch (Exception e) {
                    log.error("Failed to extract userId from token", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\": \"Invalid token\"}"));
                }
            }

            // Build URI with query parameters if needed
            String fullUrl = chatServiceUrl + path;
            if (uriCustomizer != null) {
                org.springframework.web.util.UriComponentsBuilder builder = 
                    org.springframework.web.util.UriComponentsBuilder.fromUriString(fullUrl);
                uriCustomizer.accept(builder);
                fullUrl = builder.build().toUriString();
            }

            log.debug("Forwarding {} request to: {} with userId: {}", method, fullUrl, userId);

            // Forward request to chat microservice
            WebClient.RequestBodySpec requestSpec = webClientBuilder.build()
                .method(org.springframework.http.HttpMethod.valueOf(method))
                .uri(fullUrl)
                .header("x-user-id", String.valueOf(userId))
                .header(HttpHeaders.CONTENT_TYPE, "application/json");

            if (body != null) {
                requestSpec.bodyValue(body);
            }

            return requestSpec
                .retrieve()
                .toEntity(String.class)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("Error from chat service: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
                    return Mono.just(ResponseEntity.status(ex.getStatusCode())
                        .body(ex.getResponseBodyAsString()));
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("Unexpected error forwarding request to chat service", ex);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("{\"error\": \"Internal server error\"}"));
                });

        } catch (Exception e) {
            log.error("Error processing request", e);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\": \"Internal server error\"}"));
        }
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Extract userId from JWT token
     */
    private Long extractUserIdFromToken(String token) {
        // Validate token by extracting username
        tokenService.extractUsername(token);
        
        // Get person from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Person) {
            Person person = (Person) authentication.getPrincipal();
            return person.getId();
        }
        
        // If not in security context, this should not happen as filter already validated
        throw new IllegalStateException("User not found in security context");
    }
}
