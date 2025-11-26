package glue.Gachi_Sanchaek.common.util;


import glue.Gachi_Sanchaek.common.security.jwt.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ControllerLoggingAspect {

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object logControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String userId = getUserId();

        String methodName = joinPoint.getSignature().toShortString();
        log.info("API 실행 시작: {} | userId: {}", methodName, userId);

        long startTime = System.currentTimeMillis();
        Object result;
        try {
            result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            log.info("API 실행 성공: {} | userId: {} | 실행 시간: {} ms", methodName, userId, duration);
            return result;
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("API 실행 중 오류 발생: {} | userId: {} | 실행 시간: {} ms - {}", methodName, userId, duration, e.getMessage());
            throw e;
        }
    }

    private static String getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = null;
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails userDetails) {
                username = userDetails.getUsername();
            } else if (principal instanceof String) {
                username = (String) principal;
            }
        }
        return username;
    }
}