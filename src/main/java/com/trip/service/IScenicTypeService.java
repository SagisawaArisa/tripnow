package com.trip.service;

import com.trip.dto.Result;
import com.trip.entity.ScenicType;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IScenicTypeService extends IService<ScenicType> {
    Result queryTypeList();
}
