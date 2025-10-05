package com.trip.service.impl;

import com.trip.dto.Result;
import com.trip.entity.ScenicType;
import com.trip.mapper.ScenicTypeMapper;
import com.trip.service.IScenicTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.utils.CacheClient;
import com.trip.constants.RedisScenicConstants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ScenicTypeServiceImpl extends ServiceImpl<ScenicTypeMapper, ScenicType> implements IScenicTypeService {

    @Resource
    private CacheClient cacheClient;


    @Override
    public Result queryTypeList() {

        List<ScenicType> typeList = cacheClient.queryListWithPassThrough(RedisScenicConstants.CACHE_SCENIC_TYPE_KEY,"",ScenicType.class,() -> this.query().orderByAsc("sort").list(),45L, TimeUnit.MINUTES);
        if (!typeList.isEmpty()) {
            return Result.ok(typeList);
        }
        return Result.fail("服务器繁忙，请稍后再试");
    }

}
