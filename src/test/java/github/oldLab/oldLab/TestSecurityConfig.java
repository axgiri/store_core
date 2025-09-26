package github.oldLab.oldLab;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import github.oldLab.oldLab.serviceImpl.AccessControlService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * Test security configuration for tests.
 *
 * Instead of registering a second SecurityFilterChain that matches `anyRequest()` (which conflicts with the
 * application's own chain), we replace the JWT filter with a test stub that sets an authenticated principal on
 * every request. Method-level `@PreAuthorize` checks are satisfied by the permissive AccessControlService below.
 */
@TestConfiguration
public class TestSecurityConfig {

    /**
     * Provide a test AsyncJwtFilter implementation so the application's SecurityConfiguration can
     * autowire it. This subclass simply authenticates every request as a user with ROLE_ADMIN.
     */
    @Bean
    @Primary
    public github.oldLab.oldLab.configuration.AsyncJwtFilter testAsyncJwtFilter() {
        return new TestAsyncJwtFilter();
    }

    static class TestAsyncJwtFilter extends github.oldLab.oldLab.configuration.AsyncJwtFilter {
        TestAsyncJwtFilter() {
            super(null, null);
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
            var auth = new UsernamePasswordAuthenticationToken("loadtest", null, authorities);
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
            try {
                filterChain.doFilter(request, response);
            } finally {
                SecurityContextHolder.clearContext();
            }
        }
    }

    @Bean
    @Primary
    public AccessControlService permissiveAccessControlService() {
        return new AccessControlServiceStub();
    }

    // lightweight standalone stub implementation used only in tests
    static class AccessControlServiceStub extends AccessControlService {
        AccessControlServiceStub() {
            // bypass real dependencies -- the tests only need boolean checks to pass
            super(null, null, null);
        }

        @Override
        public boolean isAdmin(org.springframework.security.core.Authentication authentication) { return true; }

        @Override
        public boolean isModerator(org.springframework.security.core.Authentication authentication) { return true; }

        @Override
        public boolean isSelf(org.springframework.security.core.Authentication authentication, Long personId) { return true; }

        @Override
        public boolean isSelfByEmail(org.springframework.security.core.Authentication authentication, String email) { return true; }

        @Override
        public boolean isCompanyWorker(org.springframework.security.core.Authentication authentication, Long companyId) { return true; }

        @Override
        public boolean isCompanyWorkerByProduct(org.springframework.security.core.Authentication authentication, Long productId) { return true; }

        @Override
        public boolean hasCompany(org.springframework.security.core.Authentication authentication) { return true; }

        @Override
        public boolean isReviewOwner(org.springframework.security.core.Authentication authentication, Long reviewId) { return true; }
    }
}
