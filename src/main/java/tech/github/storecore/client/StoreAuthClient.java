package tech.github.storecore.client;

import java.util.function.BooleanSupplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import tech.github.storecore.exception.ServiceCommunicationException;

@Slf4j
@Component
public class StoreAuthClient {

    private static final String CB_STORE_AUTH = "store-auth";

    private final RestClient restClient;

    @Value("${api.service.store-authentication.validate}")
    private String validateEmailPath;

    public StoreAuthClient(@Value("${api.service.store-authentication.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @CircuitBreaker(name = CB_STORE_AUTH, fallbackMethod = "validateEmailFallback")
    public boolean validateEmail(String email) {
        log.debug("Validating email: {}", email);
        return executeRequest(() -> restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(validateEmailPath)
                        .queryParam("email", email)
                        .build())
                .retrieve()
                .body(Boolean.class), "Failed to validate email");
    }

    @SuppressWarnings("unused") //reflection api uses this method as fallback
    private boolean validateEmailFallback(String email, Throwable t) {
        log.warn("Circuit breaker fallback for validateEmail, email={}: {}", email, t.getMessage());
        throw new ServiceCommunicationException(
                "Authentication service is temporarily unavailable, please try again later", t);
    }

    private boolean executeRequest(BooleanSupplier request, String errorMessage) {
        try {
            return request.getAsBoolean();
        } catch (Exception e) {
            throw new ServiceCommunicationException(errorMessage, e);
        }
    }
}