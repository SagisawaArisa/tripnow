package com.trip.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.entity.SeckillOrder;
import com.trip.mapper.SeckillOrderMapper;
import com.trip.service.ISeckillOrderService;
import com.trip.service.ISeckillVoucherService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

@Slf4j
@Service
public class SeckillOrderServiceImpl extends ServiceImpl<SeckillOrderMapper, SeckillOrder> implements ISeckillOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private RedissonClient redissonClient;

    @Override
    @Transactional
    public void createSeckillOrder(SeckillOrder seckillOrder) {
        Long userId = seckillOrder.getUserId();
        Long voucherId = seckillOrder.getVoucherId();
            try{
            boolean success = seckillVoucherService.update()
                    .setSql("stock = stock - 1")
                    .eq("voucher_id", voucherId).gt("stock", 0)
                    .update();
            if (!success) {
                log.error("库存不足！");
                return;
            }
            save(seckillOrder);
        } catch (DuplicateKeyException e) {
            log.warn("用户已存在订单，重复下单被阻止，userId={}, voucherId={}", userId, voucherId);
        }
    }
}
