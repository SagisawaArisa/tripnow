package com.trip.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trip.dto.Result;
import com.trip.entity.Scenic;
import com.trip.annotation.RateLimit;
import com.trip.service.IScenicService;
import com.trip.constants.SystemConstants;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/scenic")
public class ScenicController {

    @Resource
    public IScenicService scenicService;

    /**
     * 根据id查询景区信息
     * @param id 景区id
     * @return 景区详情数据
     */
    @GetMapping("/{id}")
    public Result queryScenicById(@PathVariable("id") Long id) {
        return scenicService.queryById(id);
    }

    /**
     * 新增景区信息
     * @param scenic 景区数据
     * @return 景区id
     */
    @PostMapping
    public Result saveScenic(@RequestBody Scenic scenic) {
        scenicService.save(scenic);
        return Result.ok(scenic.getId());
    }

    /**
     * 更新景区信息
     * @param scenic 景区数据
     * @return 无
     */
    @PutMapping
    public Result updateScenic(@RequestBody Scenic scenic) {
        return scenicService.update(scenic);
    }

    /**
     * 根据类别分页查询景区信息
     * @param typeId 类别id
     * @param current 页码
     * @return 景区列表
     */
    @GetMapping("/of/type")
    public Result queryScenicByType(
            @RequestParam("typeId") Integer typeId,
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "x", required = false) Double x,
            @RequestParam(value = "y", required = false) Double y
    ) {
       return scenicService.queryScenicByType(typeId, current, x, y);
    }

    /**
     * 根据名称关键字分页查询景区信息
     * @param name 名称关键字
     * @param current 页码
     * @return 景区列表
     */
    @GetMapping("/of/name")
    public Result queryScenicByName(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "current", defaultValue = "1") Integer current
    ) {
        Page<Scenic> page = scenicService.query()
                .like(StrUtil.isNotBlank(name), "name", name)
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        return Result.ok(page.getRecords());
    }

    /**
     * 全文检索景区信息
     * @param key 搜索关键字
     * @param page 页码
     * @param size 每页大小
     * @return 景区列表
     */
    @GetMapping("/search")
    @RateLimit(key = "scenic_search", count = 20, time = 60)
    public Result searchScenic(@RequestParam(value = "key", required = false) String key,
                               @RequestParam(value = "area", required = false) String area,
                               @RequestParam(value = "sortBy", required = false) String sortBy,
                               @RequestParam(value = "lat", required = false) Double lat,
                               @RequestParam(value = "lon", required = false) Double lon,
                               @RequestParam(value = "page", defaultValue = "1") Integer page,
                               @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return scenicService.search(key, area, sortBy, lat, lon, page, size);
    }

    /**
     * 附近热门景区搜索
     * @param lat 纬度
     * @param lon 经度
     * @param distance 距离范围（km），默认 50
     * @param page 页码
     * @param size 每页大小
     * @return 景区列表
     */
    @GetMapping("/hot-nearby")
    public Result queryHotScenicNearby(@RequestParam("lat") Double lat,
                               @RequestParam("lon") Double lon,
                               @RequestParam(value = "distance", defaultValue = "50") Integer distance,
                               @RequestParam(value = "page", defaultValue = "1") Integer page,
                               @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return scenicService.queryHotScenicNearby(lat, lon, distance, page, size);
    }
}
