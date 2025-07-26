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
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        String jwt = header.substring(7);

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        AsyncContext asyncCtx = request.startAsync();
        asyncCtx.setTimeout(5000);

        asyncExecutor.execute(() -> {
            try {
                String username = tokenService.extractUsername(jwt);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    Claims claims = tokenService.extractAllClaimsAsync(jwt).join();
                    log.debug("Extracted claims: {}", claims);
                    List<SimpleGrantedAuthority> auths = ((List<?>) claims.get("roles"))
                        .stream()
                        .map(r -> new SimpleGrantedAuthority((String) r))
                        .collect(Collectors.toList());

                    if (tokenService.isTokenValid(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, auths);
                        authToken.setDetails(new WebAuthenticationDetailsSource()
                            .buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
                filterChain.doFilter(asyncCtx.getRequest(), asyncCtx.getResponse());
            } catch (Exception e) {
                log.debug(("async JWT processing failed"));
                try {
                    filterChain.doFilter(asyncCtx.getRequest(), asyncCtx.getResponse());
                } catch (Exception ex) {
                    log.error("error in fallback filterChain", ex);
                }
            } finally {
                asyncCtx.complete();
            }
        });
    }
}
