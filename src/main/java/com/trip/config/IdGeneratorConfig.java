package com.trip.config;

import com.github.yitter.contract.IdGeneratorOptions;
import com.github.yitter.idgen.YitIdHelper;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class IdGeneratorConfig {

    @PostConstruct
    public void init() {
        // 创建 IdGeneratorOptions 对象，请在构造函数中输入 WorkerId：
        // WorkerId 应该从配置中心或环境变量中获取，这里暂时直接写死为 1
        short workerId = 1; 
        IdGeneratorOptions options = new IdGeneratorOptions(workerId);
        options.BaseTime = 1672531200000L; // 设置基准时间 2023-01-01 08:00:00
        // options.WorkerIdBitLength = 10; // WorkerIdBitLength 默认值6，支持的 WorkerId 最大值为2^6-1，若 WorkerId 超过64，可设置该值
        // options.SeqBitLength = 6; // SeqBitLength 默认值6，支持同一毫秒生成 2^6=64 个ID，若需更高并发，可设置该值
        
        // 保存参数
        YitIdHelper.setIdGenerator(options);
    }
}
