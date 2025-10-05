package com.trip.constants;

public final class RedisSeckillConstants {
    private RedisSeckillConstants() {
    }

    public static final String SECKILL_STOCK_PREFIX = "seckill:stock:";

    public static String getSeckillStockKey(Long voucherId) {
        return SECKILL_STOCK_PREFIX + voucherId;
    }
}
