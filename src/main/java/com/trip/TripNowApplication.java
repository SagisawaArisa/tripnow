package com.trip;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.trip.mapper")
@SpringBootApplication(exclude = {
    // 禁用 Spring Boot 自带的 ES 自动配置
    org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchClientAutoConfiguration.class
})
public class TripNowApplication {
    public static void main(String[] args) {
        SpringApplication.run(TripNowApplication.class, args);
    }
}
