package com.trip.constants;

import java.time.Duration;

public final class RedisScenicConstants {
    private RedisScenicConstants() {
    }

    public static final String CACHE_SCENIC_PREFIX = "cache:scenic:";
    public static final String CACHE_SCENIC_TYPE_KEY = "cache:scenic:type";
    public static final Duration CACHE_SCENIC_TTL = Duration.ofMinutes(30);

    public static final String SCENIC_GEO_PREFIX = "scenic:geo:";
    public static final String SCENIC_BLOG_PREFIX = "scenic:blog:";

    public static final int SCENIC_BLOG_PREVIEW_LIMIT = 50;
    public static final int SCENIC_BLOG_PREVIEW_FETCH = 5;

    public static String getCacheScenicKey(Long scenicId) {
        return CACHE_SCENIC_PREFIX + scenicId;
    }

    public static String getScenicGeoKey(Long typeId) {
        return SCENIC_GEO_PREFIX + typeId;
    }

    public static String getScenicBlogKey(Long scenicId) {
        return SCENIC_BLOG_PREFIX + scenicId;
    }
}
