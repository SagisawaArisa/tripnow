package com.trip.constants;

import java.time.Duration;

public final class RedisLoginConstants {
    private RedisLoginConstants() {
    }

    public static final String LOGIN_CODE_PREFIX = "login:code:";
    public static final Duration LOGIN_CODE_TTL = Duration.ofMinutes(2);

    public static final String LOGIN_USER_PREFIX = "login:token:";
    public static final Duration LOGIN_USER_TTL = Duration.ofMinutes(30); // Access Token 30 min

    public static final String LOGIN_REFRESH_PREFIX = "login:refresh:";
    public static final Duration LOGIN_REFRESH_TTL = Duration.ofDays(7); // Refresh Token 7 days

    public static final String LOGIN_BLACKLIST_PREFIX = "login:blacklist:";

    public static String getLoginCodeKey(String phone) {
        return LOGIN_CODE_PREFIX + phone;
    }

    public static String getLoginUserKey(String token) {
        return LOGIN_USER_PREFIX + token;
    }

    public static String getRefreshTokenKey(String token) {
        return LOGIN_REFRESH_PREFIX + token;
    }

    public static String getBlacklistKey(String token) {
        return LOGIN_BLACKLIST_PREFIX + token;
    }
}
