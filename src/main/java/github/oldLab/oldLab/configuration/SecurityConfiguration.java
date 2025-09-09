package github.oldLab.oldLab.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final AsyncJwtFilter jwtFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Persons
                .requestMatchers(HttpMethod.POST,
                        "/api/v1/persons/async/signup",
                        "/api/v1/persons/login",
                        "/api/v1/persons/refresh",
                        "/api/v1/persons/requestPasswordReset",
                        "/api/v1/persons/resetPassword")
                    .permitAll()
                .requestMatchers(HttpMethod.GET,
                        "/api/v1/persons/validate",
                        "/api/v1/persons/getRoleName")
                    .permitAll()

                // Activate (OTP / login)
                .requestMatchers(HttpMethod.POST,
                        "/api/v1/activate/activate",
                        "/api/v1/activate/send/activate/*",
                        "/api/v1/activate/resend/activate/*",
                        "/api/v1/activate/login",
                        "/api/v1/activate/send/login/*")
                    .permitAll()

                // Product read-only endpoints
                .requestMatchers(HttpMethod.GET,
                        "/api/v1/products/list",
                        "/api/v1/products/search/**",
                        "/api/v1/products/*",
                        "/api/v1/products/shop/*",
                        "/api/v1/products/shop/*/search")
                    .permitAll()

                // Reviews
                .requestMatchers(HttpMethod.GET,
                        "/api/v1/reviews/shop/*",
                        "/api/v1/reviews/person/*")
                    .permitAll()

                // Shops
                .requestMatchers(HttpMethod.GET,
                        "/api/v1/shops",
                        "/api/v1/shops/category",
                        "/api/v1/shops/*")
                    .permitAll()

                // Photos
                .requestMatchers(HttpMethod.GET,
                        "/api/v1/photos/persons/**",
                        "/api/v1/photos/shops/**")
                    .permitAll()

                // Everything else requires auth
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}