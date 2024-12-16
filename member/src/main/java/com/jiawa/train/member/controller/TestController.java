package com.jiawa.train.member.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope // 支持动态刷新; 按规范的好处：以后想换配置中心，不用动java代码
public class TestController {
    @Value("${test.nacos}")
    private String testNacos;

    @Autowired
    Environment environment;
    @GetMapping("/hello")
    public String hello() {
        String port = environment.getProperty("local.server.port");
        return "hello"+ " "+testNacos+" "+ "端口："+port;
    }
}
