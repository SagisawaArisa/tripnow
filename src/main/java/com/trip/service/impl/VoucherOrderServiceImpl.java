package com.trip.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.constants.MQTopics;
import com.trip.dto.Result;
import com.trip.entity.VoucherOrder;
import com.trip.mapper.VoucherOrderMapper;
import com.trip.service.ISeckillVoucherService;
import com.trip.service.IVoucherOrderService;
import com.trip.service.IVoucherService;
import com.trip.utils.UserHolder;
import com.trip.entity.Voucher;
import com.trip.entity.SeckillOrder;
import com.github.yitter.idgen.YitIdHelper;
import lombok.extern.slf4j.Slf4j;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Collections;

@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private IVoucherService voucherService;

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }


    @Override
    public Result addVoucherOrder(Long voucherId) {
        Voucher voucher = voucherService.getById(voucherId);
        if (voucher == null) {
            return Result.fail("门票不存在！");
        }
        // 判断是否是普通凭证
        if (voucher.getType() != 0) {
            return Result.fail("请使用秒杀通道购买！");
        }
        
        // 直接下单
        Long userId = UserHolder.getUser().getId();
        
        VoucherOrder voucherOrder = new VoucherOrder();
        long orderId = YitIdHelper.nextId();
        voucherOrder.setId(orderId);
        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);
        voucherOrder.setPayType(1);
        voucherOrder.setStatus(1);
        
        save(voucherOrder);
        return Result.ok(orderId);
    }

    @Override
    public Result seckillVoucher(Long voucherId) {
        Long userId = UserHolder.getUser().getId();
        long orderId = YitIdHelper.nextId();
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(), userId.toString()
        );
        
        int r = result.intValue();
        if (r != 0) {
            return Result.fail(r == 1 ? "库存不足" : "不能重复下单");
        }
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setId(orderId);
        seckillOrder.setUserId(userId);
        seckillOrder.setVoucherId(voucherId);
        rocketMQTemplate.convertAndSend(MQTopics.SECKILL_TOPIC, seckillOrder);
        return Result.ok(orderId);
    }


}
