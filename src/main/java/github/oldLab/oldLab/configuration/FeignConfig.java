package github.oldLab.oldLab.configuration;

import feign.FeignException;
import feign.Logger;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Configuration
@EnableFeignClients
public class FeignConfig {

    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMillis(3000 + (long) (Math.random() * 5000)))
                .permittedNumberOfCallsInHalfOpenState(3)
                .slidingWindowSize(10)
                .recordExceptions(FeignException.class, TimeoutException.class)
                .build();
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.of(circuitBreakerConfig());
    }

    @Bean
    public CircuitBreaker notificationCircuitBreaker() {
        return circuitBreakerRegistry().circuitBreaker("notificationService");
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
