package com.trip.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.dto.Result;
import com.trip.entity.SeckillVoucher;
import com.trip.entity.Voucher;
import com.trip.mapper.VoucherMapper;
import com.trip.service.ISeckillVoucherService;
import com.trip.service.IVoucherService;
import com.trip.constants.RedisSeckillConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryVoucherOfScenic(Long scenicId) {
        List<Voucher> vouchers = getBaseMapper().queryVoucherOfScenic(scenicId);
        return Result.ok(vouchers);
    }

    @Override
    @Transactional
    public void addSeckillVoucher(Voucher voucher) {
        save(voucher);
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucherService.save(seckillVoucher);
        stringRedisTemplate.opsForValue().set(RedisSeckillConstants.getSeckillStockKey(voucher.getId()), voucher.getStock().toString());
    }
}
