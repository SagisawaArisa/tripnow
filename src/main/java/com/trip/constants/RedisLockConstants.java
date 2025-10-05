package com.trip.constants;

import java.time.Duration;

public final class RedisLockConstants {
    private RedisLockConstants() {
    }

    public static final String LOCK_SCENIC_PREFIX = "lock:scenic:";
    public static final Duration LOCK_SCENIC_TTL = Duration.ofSeconds(10);

    public static String getLockScenicKey(Long scenicId) {
        return LOCK_SCENIC_PREFIX + scenicId;
    }
}
