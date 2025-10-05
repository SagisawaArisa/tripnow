package com.trip;

import com.trip.service.impl.BlogServiceImpl;
import com.trip.service.impl.ScenicServiceImpl;
import com.trip.utils.CacheClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import jakarta.annotation.Resource;

@SpringBootTest
class TripNowApplicationTests {

    @Resource
    private CacheClient cacheClient;

    @Resource
    private ScenicServiceImpl scenicService;

    @Resource
    private BlogServiceImpl blogService;


    @Resource
    private StringRedisTemplate stringRedisTemplate;


}
