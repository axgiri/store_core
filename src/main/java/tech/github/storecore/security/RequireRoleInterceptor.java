package tech.github.storecore.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import tech.github.storecore.exception.ForbiddenException;
import tech.github.storecore.exception.UnauthorizedException;

@Slf4j
@Component
public class RequireRoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireRole annotation = resolveAnnotation(handlerMethod);
        if (annotation == null) {
            return true;
        }

        Object attribute = request.getAttribute(AuthenticationFilter.ATTRIBUTE_KEY);
        if (!(attribute instanceof AuthenticatedUser user)) {
            throw new UnauthorizedException("authentication required");
        }

        for (UserRole role : annotation.value()) {
            if (user.role() == role) {
                return true;
            }
        }

        log.warn("access denied: user={} role={} required={}",
                user.userId(), user.role(), annotation.value());
        throw new ForbiddenException("insufficient privileges");
    }

    private RequireRole resolveAnnotation(HandlerMethod handlerMethod) {
        RequireRole methodLevel = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (methodLevel != null) {
            return methodLevel;
        }
        return handlerMethod.getBeanType().getAnnotation(RequireRole.class);
    }
}
