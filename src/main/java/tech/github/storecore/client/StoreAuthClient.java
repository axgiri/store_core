package tech.github.storecore.client;

import java.util.function.BooleanSupplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;
import tech.github.storecore.exception.ServiceCommunicationException;

@Slf4j
@Component
public class StoreAuthClient {

    private final RestClient restClient;

    @Value("${api.service.store-authentication.validate}")
    private String validateEmailPath;

    public StoreAuthClient(@Value("${api.service.store-authentication.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public boolean validateEmail(String email) {
        log.debug("Validating email: {}", email);
        return executeRequest(() -> restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(validateEmailPath)
                        .queryParam("email", email)
                        .build())
                .retrieve()
                .body(Boolean.class),"Failed to validate email");
    }

    private boolean executeRequest(BooleanSupplier request, String errorMessage) {
        try {
            return request.getAsBoolean();
        } catch (Exception e) {
            throw new ServiceCommunicationException(errorMessage, e);
        }
    }
}