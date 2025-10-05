package com.trip.mq.consumer;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.trip.constants.MQTopics;
import com.trip.constants.RedisScenicConstants;
import com.trip.entity.Scenic;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RocketMQMessageListener(topic = MQTopics.SCENIC_SYNC_TOPIC, consumerGroup = MQTopics.SCENIC_REDIS_SYNC_GROUP)
public class ScenicRedisSyncConsumer implements RocketMQListener<String> {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void onMessage(String message) {
        log.info("Receive binlog scenic redis sync message: {}", message);
        JSONObject json = JSON.parseObject(message);
        JSONArray data = json.getJSONArray("data");

        if (data == null || data.isEmpty()) {
            return;
        }

        for (int i = 0; i < data.size(); i++) {
            JSONObject row = data.getJSONObject(i);
            Scenic scenic = row.toJavaObject(Scenic.class);
            Long id = scenic.getId();
            
            String cacheKey = RedisScenicConstants.getCacheScenicKey(id);
            stringRedisTemplate.delete(cacheKey);
        }
    }
    
}
