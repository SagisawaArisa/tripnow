package com.trip.constants;

public class MQTopics {
    private MQTopics() {
    }

    public static final String SECKILL_TOPIC = "seckill-topic";
    public static final String SECKILL_GROUP = "seckill-group";


    public static final String SCENIC_SYNC_TOPIC = "scenic-sync-topic";
    public static final String SCENIC_ES_SYNC_GROUP = "scenic-es-group";
    public static final String SCENIC_REDIS_SYNC_GROUP = "scenic-redis-group";
}
