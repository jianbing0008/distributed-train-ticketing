package com.jiawa.train.batch.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "business", url = "http://127.0.0.1:8002/business") //表示这是一个Feign客户端，用于调用业务服务
public interface BusinessFeign {

    @GetMapping("/hello")
    String hello();
}
