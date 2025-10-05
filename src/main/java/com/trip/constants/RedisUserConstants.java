package com.trip.constants;

public final class RedisUserConstants {
    private RedisUserConstants() {
    }

    public static final String USER_SIGN_PREFIX = "sign:";

    public static String getUserSignKey(Long userId, String dateSuffix) {
        return USER_SIGN_PREFIX + userId + dateSuffix;
    }
}
