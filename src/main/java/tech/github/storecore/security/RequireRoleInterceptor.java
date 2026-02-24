
package tech.github.storecore.security;

import java.util.Arrays;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tech.axgiri.jwtstore.common.dto.Payload;
import tech.github.storecore.exception.ForbiddenException;
import tech.github.storecore.exception.UnauthorizedException;

@Component
public class RequireRoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireRole methodAnnotation = handlerMethod.getMethodAnnotation(RequireRole.class);
        RequireRole classAnnotation = handlerMethod.getBeanType().getAnnotation(RequireRole.class);

        RequireRole annotation = methodAnnotation != null ? methodAnnotation : classAnnotation;

        if (annotation == null) {
            return true;
        }

        Payload payload = (Payload) request.getAttribute("jwt.payload");
        if (payload == null) {
            throw new UnauthorizedException("authentication required");
        }

        UserRole userRole = UserRole.valueOf(payload.roles().toUpperCase());
        boolean allowed = Arrays.asList(annotation.value()).contains(userRole);

        if (!allowed) {
            throw new ForbiddenException("insufficient permissions");
        }

        return true;
    }
}
