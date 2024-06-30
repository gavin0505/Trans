package cc.forim.trans.shorturl.infra.aop;

import cc.forim.trans.shorturl.infra.domain.RequestContext;
import cc.forim.trans.shorturl.infra.utils.RequestContextHolder;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 安全校验切面
 *
 * @author Gavin Zhang
 * @version V1.0
 */
@Aspect
@Component
public class SecurityAspect {

    @Pointcut("execution(* cc.forim.trans.shorturl.controller..*(..))")
    public void controllerMethods() {
    }

    @Before("controllerMethods()")
    public void checkUserType() {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(org.springframework.web.context.request.RequestContextHolder.getRequestAttributes())).getRequest();

        String userType = request.getHeader("userType");
        Long userId = Long.parseLong(request.getHeader("userId"));
        String traceId = request.getHeader("traceId");
        RequestContext context = new RequestContext(userType, userId, traceId);

        RequestContextHolder.setContext(context);
    }

    @AfterReturning("controllerMethods()")
    @AfterThrowing("controllerMethods()")
    public void clearUserContext() {
        RequestContextHolder.clear();
    }
}
