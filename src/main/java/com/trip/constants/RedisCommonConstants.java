package com.trip.constants;

import java.time.Duration;

public final class RedisCommonConstants {
    private RedisCommonConstants() {
    }

    public static final Duration CACHE_NULL_TTL = Duration.ofMinutes(2);
}
