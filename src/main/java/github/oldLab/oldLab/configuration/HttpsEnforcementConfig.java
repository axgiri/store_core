package github.oldLab.oldLab.configuration;

import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * HTTPS Enforcement Configuration for Production
 * 
 * When behind reverse proxy (nginx/Traefik) with TLS termination:
 * - App runs on HTTP internally (port 8080)
 * - Proxy handles HTTPS externally and sets X-Forwarded-Proto
 * - This config ensures Spring Boot respects forwarded headers
 * - All generated URLs (redirects, OAuth callbacks) use https://
 * 
 * Best Practice: Use reverse proxy for TLS termination instead of
 * embedding certificates in application layer (separation of concerns)
 */
@Configuration
@Profile("prod")
public class HttpsEnforcementConfig {

    /**
     * Configure Tomcat to trust X-Forwarded-* headers from reverse proxy
     * This ensures Spring Security generates HTTPS URLs for redirects
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> {
            factory.addContextCustomizers(context -> {
                // Mark all requests as requiring confidential transport
                // When X-Forwarded-Proto=https is present, Tomcat will honor it
                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                
                context.addConstraint(securityConstraint);
            });
        };
    }
}
