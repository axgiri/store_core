package tech.github.storecore.security;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_ROLE = "X-User-Role";
    public static final String ATTRIBUTE_KEY = AuthenticatedUser.class.getName();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        populateAttribute(request);
        chain.doFilter(request, response);
    }

    private void populateAttribute(HttpServletRequest request) {
        String userIdHeader = request.getHeader(HEADER_USER_ID);
        String roleHeader = request.getHeader(HEADER_USER_ROLE);

        if (userIdHeader == null || userIdHeader.isBlank()) {
            return;
        }

        try {
            UUID userId = UUID.fromString(userIdHeader.trim());
            UserRole role = (roleHeader != null && !roleHeader.isBlank())
                    ? UserRole.valueOf(roleHeader.trim().toUpperCase())
                    : UserRole.USER;

            request.setAttribute(ATTRIBUTE_KEY, new AuthenticatedUser(userId, role));
            log.trace("authenticated user set: userId={}, role={}", userId, role);
        } catch (IllegalArgumentException ex) {
            log.warn("malformed gateway headers: X-User-Id='{}', X-User-Role='{}'",
                    userIdHeader, roleHeader);
        }
    }
}
