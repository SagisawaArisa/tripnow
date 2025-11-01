package com.trip.config;

import com.trip.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class WebExceptionAdvice {

    @ExceptionHandler(com.trip.exception.RateLimitException.class)
    public Result handleRateLimitException(com.trip.exception.RateLimitException e) {
        log.warn("Rate limit caught: {}", e.getMessage());
        return Result.fail("请求过于频繁，请稍后再试");
    }

    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e) {
        log.error(e.toString(), e);
        return Result.fail("服务器异常");
    }
}
