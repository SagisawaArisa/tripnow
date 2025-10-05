package com.trip.service;

import com.trip.dto.Result;
import com.trip.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IVoucherOrderService extends IService<VoucherOrder> {

    Result seckillVoucher(Long voucherId);

    Result addVoucherOrder(Long voucherId);
}
