package com.trip.service;

import com.trip.entity.SeckillOrder;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ISeckillOrderService extends IService<SeckillOrder> {
    void createSeckillOrder(SeckillOrder seckillOrder);
}
