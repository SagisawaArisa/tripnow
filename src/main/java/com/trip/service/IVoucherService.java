package com.trip.service;

import com.trip.dto.Result;
import com.trip.entity.Voucher;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IVoucherService extends IService<Voucher> {

    Result queryVoucherOfScenic(Long scenicId);

    void addSeckillVoucher(Voucher voucher);
}
