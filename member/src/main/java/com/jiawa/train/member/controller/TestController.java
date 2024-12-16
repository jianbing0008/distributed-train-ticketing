package com.jiawa.train.member.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope // 支持动态刷新; 按规范的好处：以后想换配置中心，不用动java代码
public class TestController {
    @Value("${test.nacos}")
    private String testNacos;
    @GetMapping("/hello")
    public String hello() {
        return "hello"+ testNacos;
    }
}
