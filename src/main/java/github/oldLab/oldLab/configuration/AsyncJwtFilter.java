package github.oldLab.oldLab.configuration;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import github.oldLab.oldLab.serviceImpl.TokenServiceImpl;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncJwtFilter extends OncePerRequestFilter {

    private final TokenServiceImpl tokenService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            String username = tokenService.extractUsername(jwt);
            if (username == null) {
                log.warn("Invalid JWT - unable to extract username");
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid token");
                return;
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (!tokenService.isTokenValid(jwt, userDetails)) {
                log.warn("Invalid JWT for user: {}", username);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid token");
                return;
            }

            Claims claims = tokenService.extractAllClaimsAsync(jwt).join();
            List<SimpleGrantedAuthority> authorities = ((List<?>) claims.get("roles"))
                    .stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            authorities);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (Exception e) {
            log.error("JWT Authentication Error: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Authentication failed");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
