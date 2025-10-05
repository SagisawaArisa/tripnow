package com.trip.mq.consumer;

import com.trip.service.ISeckillOrderService;
import com.trip.constants.MQTopics;
import com.trip.entity.SeckillOrder;

import lombok.extern.slf4j.Slf4j;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import jakarta.annotation.Resource;


@Slf4j
@Component
@RocketMQMessageListener(topic = MQTopics.SECKILL_TOPIC, consumerGroup = MQTopics.SECKILL_GROUP)
public class VoucherOrderConsumer implements RocketMQListener<SeckillOrder>{

    @Resource
    private ISeckillOrderService seckillOrderService;

    @Override
    public void onMessage(SeckillOrder order) {
        log.info("Received seckill order message: {}", order.getId());
        seckillOrderService.createSeckillOrder(order);
    }


}