package tech.github.storecore.security;

import java.util.UUID;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import jakarta.servlet.http.HttpServletRequest;
import tech.axgiri.jwtstore.common.dto.Payload;
import tech.github.storecore.exception.UnauthorizedException;

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && parameter.getParameterType().equals(AuthenticatedUser.class);
    }

    @Override
    public AuthenticatedUser resolveArgument(MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {

        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        Payload payload = (Payload) request.getAttribute("jwt.payload");

        if (payload == null) {
            throw new UnauthorizedException("authentication required");
        }

        UUID userId = UUID.fromString(payload.sub());
        UserRole role = UserRole.valueOf(payload.roles().toUpperCase());

        return new AuthenticatedUser(userId, role);
    }
}
