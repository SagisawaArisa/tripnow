package com.trip.constants;

public final class RedisBlogConstants {
    private RedisBlogConstants() {
    }

    public static final String BLOG_LIKED_PREFIX = "blog:liked:";
    public static final String FEED_PREFIX = "feed:";

    public static String getBlogLikedKey(Long blogId) {
        return BLOG_LIKED_PREFIX + blogId;
    }

    public static String getFeedKey(Long userId) {
        return FEED_PREFIX + userId;
    }
}
