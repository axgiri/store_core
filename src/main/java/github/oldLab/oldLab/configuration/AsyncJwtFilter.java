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
    @Qualifier("asyncExecutor")
    private final Executor asyncExecutor;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // 1. Проверяем наличие и формат заголовка
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("JWT Token is missing or invalid");
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Извлекаем токен
        final String jwt = authHeader.substring(7);

        AsyncContext asyncCtx = request.startAsync();
        asyncCtx.setTimeout(5000);

        asyncExecutor.execute(() -> {
            try {
                // 3. Валидация токена
                String username = tokenService.extractUsername(jwt);
                if (username == null) {
                    log.warn("Invalid JWT - unable to extract username");
                    sendErrorResponse(asyncCtx, HttpServletResponse.SC_FORBIDDEN, "Invalid token");
                    return;
                }

                // 4. Загрузка пользователя
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 5. Проверка валидности токена
                if (!tokenService.isTokenValid(jwt, userDetails)) {
                    log.warn("Invalid JWT for user: {}", username);
                    sendErrorResponse(asyncCtx, HttpServletResponse.SC_FORBIDDEN, "Invalid token");
                    return;
                }

                // 6. Извлечение claims и ролей
                Claims claims = tokenService.extractAllClaimsAsync(jwt).join();
                log.debug("Extracted claims for {}: {}", username, claims);

                List<SimpleGrantedAuthority> authorities = ((List<?>) claims.get("roles"))
                        .stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .collect(Collectors.toList());

                // 7. Установка аутентификации
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                authorities);
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("Authenticated user: {}", username);

                // 8. Продолжение цепочки фильтров
                filterChain.doFilter(asyncCtx.getRequest(), asyncCtx.getResponse());

            } catch (Exception e) {
                log.error("JWT Authentication Error: {}", e.getMessage(), e);
                sendErrorResponse(asyncCtx, HttpServletResponse.SC_FORBIDDEN,
                        "Authentication failed: " + e.getMessage());
            } finally {
                asyncCtx.complete();
            }
        });
    }

    private void sendErrorResponse(AsyncContext asyncCtx, int status, String message) {
        try {
            HttpServletResponse response = (HttpServletResponse) asyncCtx.getResponse();
            response.setStatus(status);
            response.getWriter().write(message);
            response.getWriter().flush();
        } catch (IOException e) {
            log.error("Failed to send error response", e);
        }
    }
}
