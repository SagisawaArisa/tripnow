package com.trip.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trip.entity.Voucher;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface VoucherMapper extends BaseMapper<Voucher> {

    List<Voucher> queryVoucherOfScenic(@Param("scenicId") Long scenicId);
}
