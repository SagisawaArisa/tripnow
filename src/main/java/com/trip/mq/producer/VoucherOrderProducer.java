package com.trip.mq.producer;

import com.alibaba.fastjson.JSON;
import com.trip.entity.SeckillOrder;

import java.util.Collections;

import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.Message;

@RocketMQTransactionListener
class SeckillTransactionListener implements RocketMQLocalTransactionListener {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        try {
            String orderJson = new String((byte[]) msg.getPayload());
            SeckillOrder order  = JSON.parseObject(orderJson, SeckillOrder.class);

            Long result = redisTemplate.execute(
                    SECKILL_SCRIPT,
                    Collections.emptyList(),
                    order.getVoucherId().toString(),
                    order.getUserId().toString()
            );

            if (result == 0L) {
                return RocketMQLocalTransactionState.COMMIT;
            }
            return RocketMQLocalTransactionState.ROLLBACK;
        } catch (Exception e) {
            return RocketMQLocalTransactionState.UNKNOWN;
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        String orderJson = new String((byte[]) msg.getPayload());
        SeckillOrder order  = JSON.parseObject(orderJson, SeckillOrder.class);
        String key = "seckill:order:" + order.getVoucherId();
        Boolean isMember = redisTemplate.opsForSet().isMember(key, order.getUserId().toString());
        if (Boolean.TRUE.equals(isMember)) {
            return RocketMQLocalTransactionState.COMMIT;
        }
        return RocketMQLocalTransactionState.ROLLBACK;
    }
}