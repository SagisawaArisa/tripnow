package com.trip.service;

import com.trip.dto.Result;
import com.trip.entity.Scenic;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IScenicService extends IService<Scenic> {

    Result queryById(Long id);

    Result update(Scenic scenic);

    Result queryScenicByType(Integer typeId, Integer current, Double x, Double y);

    Result search(String key, String area, String sortBy, Double lat, Double lon, Integer page, Integer size);

    Result queryHotScenicNearby(Double lat, Double lon, Integer distance, Integer page, Integer size);
}
