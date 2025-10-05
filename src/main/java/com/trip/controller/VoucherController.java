package com.trip.controller;


import com.trip.dto.Result;
import com.trip.entity.Voucher;
import com.trip.service.IVoucherService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * 
 */
@RestController
@RequestMapping("/voucher")
public class VoucherController {

    @Resource
    private IVoucherService voucherService;

    /**
     * 新增秒杀券
     * @param voucher 券信息，包含秒杀信息
     * @return 券id
     */
    @PostMapping("seckill")
    public Result addSeckillVoucher(@RequestBody Voucher voucher) {
        voucherService.addSeckillVoucher(voucher);
        return Result.ok(voucher.getId());
    }

    /**
     * 新增普通券
     * @param voucher 券信息
     * @return 券id
     */
    @PostMapping
    public Result addVoucher(@RequestBody Voucher voucher) {
        voucherService.save(voucher);
        return Result.ok(voucher.getId());
    }


    /**
     * 查询景区的券列表
     * @param scenicId 景区id
     * @return 券列表
     */
    @GetMapping("/list/{scenicId}")
    public Result queryVoucherOfScenic(@PathVariable("scenicId") Long scenicId) {
       return voucherService.queryVoucherOfScenic(scenicId);
    }
}
