package com.trip.aspect;

import com.trip.annotation.RateLimit;
import com.trip.exception.RateLimitException;
import com.trip.utils.UserHolder;
import com.trip.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.util.Collections;

@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private DefaultRedisScript<Long> limitScript;

    @PostConstruct
    public void init() {
        limitScript = new DefaultRedisScript<>();
        limitScript.setLocation(new ClassPathResource("rate_limit.lua"));
        limitScript.setResultType(Long.class);
    }

    @Around("@annotation(rateLimit)")
    public Object intercept(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = rateLimit.key();
        int count = rateLimit.count();
        int time = rateLimit.time();

        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String ip = getIpAddress(request);

            String userId = "guest";
            UserDTO user = UserHolder.getUser();
            if (user != null) {
                userId = user.getId().toString();
            }

            String combineKey = "ratelimit:" + key + ":" + ip + ":" + userId;

            Long result = stringRedisTemplate.execute(
                    limitScript,
                    Collections.singletonList(combineKey),
                    String.valueOf(count),
                    String.valueOf(time),
                    String.valueOf(System.currentTimeMillis())
            );

            if (result != null && result == 1) {
                log.warn("Rate limit exceeded for key: {}", combineKey);
                throw new RateLimitException("访问过于频繁，请稍候再试");
            }

        } catch (RateLimitException e) {
            throw e;
        } catch (Exception e) {
            log.error("Rate limiter Redis error: {}", e.getMessage());
        }

        return joinPoint.proceed();
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
