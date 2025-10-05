package com.trip.controller;


import com.trip.dto.Result;
import com.trip.service.IScenicTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/scenic-type")
public class ScenicTypeController {
    @Resource
    private IScenicTypeService typeService;

    @GetMapping("list")
    public Result queryTypeList() {
        return typeService.queryTypeList();
    }
}
