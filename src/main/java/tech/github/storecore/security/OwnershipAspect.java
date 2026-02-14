package tech.github.storecore.security;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import tech.github.storecore.exception.ForbiddenException;

@Aspect
@Component
public class OwnershipAspect {

    private final Map<ResourceType, OwnershipStrategy> strategies;

    public OwnershipAspect(List<OwnershipStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(OwnershipStrategy::supports, Function.identity()));
    }

    @Before("@annotation(verifyOwnership)")
    public void checkOwnership(JoinPoint joinPoint, VerifyOwnership verifyOwnership) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        AuthenticatedUser user = findUser(args);
        Long resourceId = findResourceId(paramNames, args, verifyOwnership.idParam());

        OwnershipStrategy strategy = strategies.get(verifyOwnership.value());
        if (strategy == null) {
            throw new IllegalStateException("no ownership strategy for: " + verifyOwnership.value());
        }

        if (!strategy.isOwner(user.userId(), resourceId)) {
            throw new ForbiddenException("you don't own this resource");
        }
    }

    private AuthenticatedUser findUser(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof AuthenticatedUser user) {
                return user;
            }
        }
        throw new IllegalStateException("@VerifyOwnership requires @CurrentUser AuthenticatedUser parameter");
    }

    private Long findResourceId(String[] paramNames, Object[] args, String idParam) {
        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equals(idParam) && args[i] instanceof Long id) {
                return id;
            }
        }
        throw new IllegalStateException("no Long parameter named '" + idParam + "' found");
    }
}
